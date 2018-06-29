pushd ./pfad-master/ || exit 1
mvn clean install || exit 1
popd
#scp ./pfad-master/pfad/target/pfad.war laappsms001:./deploy/pfad.war
#ssh laappsms001 mv ./deploy/pfad.war /opt/wildfly/wildfly101/standalone/deployments/pfad.war
scp ./pfad-master/pfad/target/pfad.war vlproap001.at.inside:./deploy/pfad.war
ssh vlproap001.at.inside rm -f /opt/jboss/pfad/wildfly/standalone/deployments/pfad.war
#sleep 5
ssh vlproap001.at.inside mv ./deploy/pfad.war /opt/jboss/pfad/wildfly/standalone/deployments/pfad.war
