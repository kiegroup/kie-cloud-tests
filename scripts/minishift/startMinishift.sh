#!/bin/bash -x

# Run Minishift
minishift start --vm-driver=virtualbox --memory 8GB --insecure-registry 0.0.0.0/0

# Enable insecure registry
minishift openshift config set --patch "{ \"imagePolicyConfig\": null }"

# Fix memory issue with indexer
minishift ssh 'sudo sh -c "echo vm.max_map_count=262144 >> /etc/sysctl.conf"'
minishift ssh 'sudo sh -c "sysctl -w vm.max_map_count=262144"'

# Run Nexus
oc login -u system:admin
oc adm policy add-cluster-role-to-user cluster-admin admin
oc new-project nexus
oc new-app sonatype/nexus3
oc expose service nexus3

# Run Gogs
oc new-project gogs
oc adm policy add-scc-to-user anyuid -z default
oc new-app -f https://raw.githubusercontent.com/OpenShiftDemos/gogs-openshift-docker/master/openshift/gogs-persistent-template.yaml --param=HOSTNAME=tmp
oc delete route gogs
oc expose service gogs --port=3000

oc login -u developer -p none
