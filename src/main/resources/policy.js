var context = $evaluation.getContext();
var permission = $evaluation.getPermission();
var identity = context.getIdentity();
var resource = permission.getResource();
var identityAttr = identity.getAttributes().toMap();
var groups = identityAttr.get('groups');
// Prefixing owner-group path with leading /, to match Keycloak groups mapping ['/group1', '/group2']
var ownerGroup = '/' + resource.getSingleAttribute('owner-group');
if (resource.getSingleAttribute('owner-group') != null && resource.getSingleAttribute('owner-group').startsWith('/')){
    ownerGroup = resource.getSingleAttribute('owner-group');
}
var groupId = '/' + resource.getSingleAttribute('groupId');
if (resource.getSingleAttribute('groupId') != null && resource.getSingleAttribute('groupId').startsWith('/')){
    groupId = resource.getSingleAttribute('groupId');
}
var groupPath = '/' + resource.getSingleAttribute('groupPath');
if (resource.getSingleAttribute('groupPath') != null && resource.getSingleAttribute('groupPath').startsWith('/')){
    groupPath = resource.getSingleAttribute('groupPath');
}
for(var i=0; i<groups.length; i++){
    print('Current User Group: ' + groups[i]);
    if(ownerGroup == groups[i] || groupId == groups[i] || groupPath == groups[i]){
       $evaluation.grant();
    }
}