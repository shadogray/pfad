pushd ./pfad-master/ || exit 1
mvn clean install || exit 1
popd
scp ./pfad-master/pfad/target/pfad.war laappsms001:./deploy/pfad.war
ssh laappsms001 mv ./deploy/pfad.war /opt/wildfly/wildfly101/standalone/deployments/pfad.war
