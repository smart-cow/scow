rem 

# may need to run this to allow deletes
set sql_safe_updates=0;

# Fix problem where processinstancelog and processinstanceinfo get out of sync.
# this removes any rows in processinstancelog that are 'active' but have no corresponding
# row in processinstanceinfo
delete FROM jbpm5.processinstancelog where end_date is null and processInstanceId not in (select id from processinstanceinfo);