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
        "summary"     : "[REGRESS][{}] Тесты падают на регрессе".format(summary),
        "reporter"    : {"name":reporter_name},
        "description" : open(description_path, "r").read(),
        "priority"    : {"name": priority}, # Highest High Medium Low Lowest Blocker
        "labels"      : labels,
        "assignee"    : {"name": assignee_name}
    }
    return issue_fields

def get_jira():
    jira_options = {'server': 'https://j-ymp.yadro.com'}
    jira = JIRA(options=jira_options, basic_auth=("a.ovchinnikova", "IreN951a"))
    return jira

def check_for_issue(summary):
    jira = get_jira()
    issues = jira.search_issues("project = JenkinsAutoBug AND summary ~ \"{}\"".format(summary))
    if issues:
        return sorted(issues, key=lambda x: x.id, reverse=True)[0]
    else:
        return None

def create_jira_issue(issue_fields):
    jira_options = {'server': 'http://172.18.0.1:7070'}
    jira = JIRA(options=jira_options, basic_auth=("a.ovchinnikova", "Rozalija"))
#    key_cert_data = open("cert_data.txt","r").read()
#    oauth_dict = {'access_token': '5QoAgLbgjqhnIF1m6V7zhSLOre2KCDjy', 'access_token_secret': 'iHFMz1Hehds4ki7QeWWCpft7YwPCJtpA', 'consumer_key': 'jira-oauth-consumer', 'key_cert': key_cert_data}
#    jira = JIRA(options=jira_options, oauth=oauth_dict)
    jira.create_issue(issue_fields)

create_jira_issue(build_custom_issue(*sys.argv[1:]))
