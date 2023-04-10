import sys
from jira import JIRA

def build_custom_issue(summary,assignee_name, description_path, 
                proj_key="JAB",
                issue_type="Bug",
                reporter_name="a.ovchinnikova",
                priority="Medium",
                labels=[]
                ):
    issue_fields = {
        "project"     : {"key": proj_key},
        "issuetype"   : {"name": issue_type},   # Bug Task Story Epic
        "summary"     : summary,
        "reporter"    : {"name":reporter_name},
        "description" : open(description_path, "r").read(),
        "priority"    : {"name": priority}, # Highest High Medium Low Lowest Blocker
        "labels"      : labels,
        "assignee"    : {"name": assignee_name}
    }
    return issue_fields

def create_jira_issue(issue_fields):
    jira_options = {'server': 'https://j-ymp.yadro.com'}
    jira = JIRA(options=jira_options, basic_auth=("a.ovchinnikova", "IreN951a"))
    jira.create_issue(issue_fields)

create_jira_issue(build_custom_issue(*sys.argv[1:]))
