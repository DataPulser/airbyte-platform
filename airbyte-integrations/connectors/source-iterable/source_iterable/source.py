#
# Copyright (c) 2021 Airbyte, Inc., all rights reserved.
#


from typing import Any, List, Mapping, Tuple

from airbyte_cdk.models import SyncMode
from airbyte_cdk.sources import AbstractSource
from airbyte_cdk.sources.streams import Stream

from .api import (
    Campaigns,
    Channels,
    EmailBounce,
    EmailClick,
    EmailComplaint,
    EmailOpen,
    EmailSend,
    EmailSendSkip,
    EmailSubscribe,
    EmailUnsubscribe,
    Lists,
    ListUsers,
    MessageTypes,
    Metadata,
    Templates,
    Users,
)


class SourceIterable(AbstractSource):
    def check_connection(self, logger, config) -> Tuple[bool, any]:
        try:
            list_gen = Lists(api_key=config["api_key"]).read_records(sync_mode=SyncMode.full_refresh)
            next(list_gen)
            return True, None
        except Exception as e:
            return False, f"Unable to connect to Iterable API with the provided credentials - {e}"

    def streams(self, config: Mapping[str, Any]) -> List[Stream]:
        return [
            Campaigns(api_key=config["api_key"]),
            Channels(api_key=config["api_key"]),
            EmailBounce(api_key=config["api_key"], start_date=config["start_date"]),
            EmailClick(api_key=config["api_key"], start_date=config["start_date"]),
            EmailComplaint(api_key=config["api_key"], start_date=config["start_date"]),
            EmailOpen(api_key=config["api_key"], start_date=config["start_date"]),
            EmailSend(api_key=config["api_key"], start_date=config["start_date"]),
            EmailSendSkip(api_key=config["api_key"], start_date=config["start_date"]),
            EmailSubscribe(api_key=config["api_key"], start_date=config["start_date"]),
            EmailUnsubscribe(api_key=config["api_key"], start_date=config["start_date"]),
            Lists(api_key=config["api_key"]),
            ListUsers(api_key=config["api_key"]),
            MessageTypes(api_key=config["api_key"]),
            Metadata(api_key=config["api_key"]),
            Templates(api_key=config["api_key"], start_date=config["start_date"]),
            Users(api_key=config["api_key"], start_date=config["start_date"]),
        ]
