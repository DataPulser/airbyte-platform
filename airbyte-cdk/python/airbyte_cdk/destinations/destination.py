#
# Copyright (c) 2021 Airbyte, Inc., all rights reserved.
#

import argparse
import io
import sys
from abc import ABC, abstractmethod
from typing import Any, Iterable, List, Mapping

from airbyte_cdk import AirbyteLogger
from airbyte_cdk.connector import Connector
from airbyte_cdk.models import AirbyteMessage, ConfiguredAirbyteCatalog, Type
from airbyte_cdk.sources.utils.schema_helpers import check_config_against_spec_or_exit
from pydantic import ValidationError


class Destination(Connector, ABC):
    logger = AirbyteLogger()
    VALID_CMDS = {"spec", "check", "write"}

    @abstractmethod
    def write(
        self, config: Mapping[str, Any], configured_catalog: ConfiguredAirbyteCatalog, input_messages: Iterable[AirbyteMessage]
    ) -> Iterable[AirbyteMessage]:
        """Implement to define how the connector writes data to the destination"""

    def _run_check(self, config: Mapping[str, Any]) -> AirbyteMessage:
        check_result = self.check(self.logger, config)
        return AirbyteMessage(type=Type.CONNECTION_STATUS, connectionStatus=check_result)

    def _parse_input_stream(self, input_stream: io.TextIOWrapper) -> Iterable[AirbyteMessage]:
        """Reads from stdin, converting to Airbyte messages"""
        for line in input_stream:
            try:
                yield AirbyteMessage.parse_raw(line)
            except ValidationError:
                self.logger.info(f"ignoring input which can't be deserialized as Airbyte Message: {line}")

    def _run_write(
        self, config: Mapping[str, Any], configured_catalog_path: str, input_stream: io.TextIOWrapper
    ) -> Iterable[AirbyteMessage]:
        catalog = ConfiguredAirbyteCatalog.parse_file(configured_catalog_path)
        input_messages = self._parse_input_stream(input_stream)
        self.logger.info("Begin writing to the destination...")
        yield from self.write(config=config, configured_catalog=catalog, input_messages=input_messages)
        self.logger.info("Writing complete.")

    def parse_args(self, args: List[str]) -> argparse.Namespace:
        """
        :param args: commandline arguments
        :return:
        """

        parent_parser = argparse.ArgumentParser(add_help=False)
        main_parser = argparse.ArgumentParser()
        subparsers = main_parser.add_subparsers(title="commands", dest="command")

        # spec
        subparsers.add_parser("spec", help="outputs the json configuration specification", parents=[parent_parser])

        # check
        check_parser = subparsers.add_parser("check", help="checks the config can be used to connect", parents=[parent_parser])
        required_check_parser = check_parser.add_argument_group("required named arguments")
        required_check_parser.add_argument("--config", type=str, required=True, help="path to the json configuration file")

        # write
        write_parser = subparsers.add_parser("write", help="Writes data to the destination", parents=[parent_parser])
        write_required = write_parser.add_argument_group("required named arguments")
        write_required.add_argument("--config", type=str, required=True, help="path to the JSON configuration file")
        write_required.add_argument("--catalog", type=str, required=True, help="path to the configured catalog JSON file")

        parsed_args = main_parser.parse_args(args)
        cmd = parsed_args.command
        if not cmd:
            raise Exception("No command entered. ")
        elif cmd not in ["spec", "check", "write"]:
            # This is technically dead code since parse_args() would fail if this was the case
            # But it's non-obvious enough to warrant placing it here anyways
            raise Exception(f"Unknown command entered: {cmd}")

        return parsed_args

    def run_cmd(self, parsed_args: argparse.Namespace) -> Iterable[AirbyteMessage]:
        cmd = parsed_args.command
        if cmd not in self.VALID_CMDS:
            raise Exception(f"Unrecognized command: {cmd}")

        spec = self.spec(self.logger)
        if cmd == "spec":
            yield AirbyteMessage(type=Type.SPEC, spec=spec)
            return
        config = self.read_config(config_path=parsed_args.config)
        check_config_against_spec_or_exit(config, spec, self.logger)

        if cmd == "check":
            yield self._run_check(config=config)
        elif cmd == "write":
            # Wrap in UTF-8 to override any other input encodings
            wrapped_stdin = io.TextIOWrapper(sys.stdin.buffer, encoding="utf-8")
            yield from self._run_write(config=config, configured_catalog_path=parsed_args.catalog, input_stream=wrapped_stdin)

    def run(self, args: List[str]):
        parsed_args = self.parse_args(args)
        output_messages = self.run_cmd(parsed_args)
        for message in output_messages:
            print(message.json(exclude_unset=True))
