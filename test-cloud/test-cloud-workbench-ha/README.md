BC HA REST tests

example of execute cmd for tests:
mvn clean install -Dopenshift.master.url=https://master.openshiftdomain:8443 -Dopenshift.username=user -Dopenshift.passwordedhat -Dgit.provider=Gogs -Dgogs.url=http://gogs.project.openshiftdomain -Dgogs.username=root -Dgogs.password=redhat -Dmaven.repo.url=http://nexus.project.openshiftdomain/repository/maven-snapshots -Dmaven.repo.username=admin -Dmaven.repo.password=admin123 -Dkie.image.streams=file:///home/jschwan/ns/jbpm-dev-is.yaml -DfailIfNoTests=false -Popenshift -Pparallel -Dtemplate.database=postgresql -Dtemplate.project=jbpm -Dopenshift.namespace.prefix=jschwan-bcha -Ddefault.domain.suffix=.project.openshiftdomain -Dldap.url=ldap://master.openshiftdomain:30389 -Dit.test=SpaceFunctionalIntegrationTest

example of command to build Kie Cloud Tests (frameworks):
$ mvn clean install -DskipTests -Dkie.app.template.url=https://raw.githubusercontent.com/jboss-container-images/rhpam-7-openshift-image/master/templates

