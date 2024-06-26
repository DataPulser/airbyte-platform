#
# Copyright (c) 2021 Airbyte, Inc., all rights reserved.
#


from typing import Any, List, Mapping, Tuple

import pendulum
from airbyte_cdk.logger import AirbyteLogger
from airbyte_cdk.models import SyncMode
from airbyte_cdk.sources import AbstractSource
from airbyte_cdk.sources.streams import Stream
from source_harvest.streams import (
    BillableRates,
    Clients,
    Company,
    Contacts,
    CostRates,
    EstimateItemCategories,
    EstimateMessages,
    Estimates,
    ExpenseCategories,
    Expenses,
    ExpensesCategories,
    ExpensesClients,
    ExpensesProjects,
    ExpensesTeam,
    InvoiceItemCategories,
    InvoiceMessages,
    InvoicePayments,
    Invoices,
    ProjectAssignments,
    ProjectBudget,
    Projects,
    Roles,
    TaskAssignments,
    Tasks,
    TimeClients,
    TimeEntries,
    TimeProjects,
    TimeTasks,
    TimeTeam,
    Uninvoiced,
    UserAssignments,
    Users,
)

from .auth import HarvestTokenAuthenticator


class SourceHarvest(AbstractSource):
    def check_connection(self, logger: AirbyteLogger, config: Mapping[str, Any]) -> Tuple[bool, Any]:
        try:
            auth = HarvestTokenAuthenticator(token=config["api_token"], account_id=config["account_id"])
            replication_start_date = pendulum.parse(config["replication_start_date"])
            users_gen = Users(authenticator=auth, replication_start_date=replication_start_date).read_records(
                sync_mode=SyncMode.full_refresh
            )
            next(users_gen)
            return True, None
        except Exception as error:
            return False, f"Unable to connect to Harvest API with the provided credentials - {repr(error)}"

    def streams(self, config: Mapping[str, Any]) -> List[Stream]:
        """
        :param config: A Mapping of the user input configuration as defined in the connector spec.
        """
        auth = HarvestTokenAuthenticator(token=config["api_token"], account_id=config["account_id"])
        replication_start_date = pendulum.parse(config["replication_start_date"])
        from_date = replication_start_date.date()

        streams = [
            Clients(authenticator=auth, replication_start_date=replication_start_date),
            Contacts(authenticator=auth, replication_start_date=replication_start_date),
            Company(authenticator=auth),
            Invoices(authenticator=auth, replication_start_date=replication_start_date),
            InvoiceMessages(authenticator=auth, replication_start_date=replication_start_date),
            InvoicePayments(authenticator=auth, replication_start_date=replication_start_date),
            InvoiceItemCategories(authenticator=auth, replication_start_date=replication_start_date),
            Estimates(authenticator=auth, replication_start_date=replication_start_date),
            EstimateMessages(authenticator=auth, replication_start_date=replication_start_date),
            EstimateItemCategories(authenticator=auth, replication_start_date=replication_start_date),
            Expenses(authenticator=auth, replication_start_date=replication_start_date),
            ExpenseCategories(authenticator=auth, replication_start_date=replication_start_date),
            Tasks(authenticator=auth, replication_start_date=replication_start_date),
            TimeEntries(authenticator=auth, replication_start_date=replication_start_date),
            UserAssignments(authenticator=auth, replication_start_date=replication_start_date),
            TaskAssignments(authenticator=auth, replication_start_date=replication_start_date),
            Projects(authenticator=auth, replication_start_date=replication_start_date),
            Roles(authenticator=auth, replication_start_date=replication_start_date),
            Users(authenticator=auth, replication_start_date=replication_start_date),
            BillableRates(authenticator=auth),
            CostRates(authenticator=auth),
            ProjectAssignments(authenticator=auth, replication_start_date=replication_start_date),
            ExpensesClients(authenticator=auth, from_date=from_date),
            ExpensesProjects(authenticator=auth, from_date=from_date),
            ExpensesCategories(authenticator=auth, from_date=from_date),
            ExpensesTeam(authenticator=auth, from_date=from_date),
            Uninvoiced(authenticator=auth, from_date=from_date),
            TimeClients(authenticator=auth, from_date=from_date),
            TimeProjects(authenticator=auth, from_date=from_date),
            TimeTasks(authenticator=auth, from_date=from_date),
            TimeTeam(authenticator=auth, from_date=from_date),
            ProjectBudget(authenticator=auth, from_date=from_date),
        ]

        return streams
