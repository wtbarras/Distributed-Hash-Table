#Author: Tiger Barras
#Makefile for cs455.overlay assignment
#Random change to test out github
JC = javac
C = .class #put this is so I stop deleting .java files....

#Aliases for directory paths
NODEPATH = ./cs455/overlay/node/
TRANSPORTPATH = ./cs455/overlay/transport/
WIREFORMATSPATH = ./cs455/overlay/wireformats/
ROUTINGPATH = ./cs455/overlay/routing/
UTILPATH = ./cs455/overlay/util/

#Alias contains files in package cs455.overlay.node
NODE = Node.class MessageNode.class Registry.class
#Alias contains files in package cs455.overlay.transport
TRANSPORT = ServerThread.class Sender.class RecieverThread.class Connection.class ConnectionCache.class RegisterConnectionCache.class NodeConnectionCache.class
#Alias contains files in package cs455.overlay.exception
EXCEPTION = ConnectionCacheException.class
#package cs455.overlay.wireformats
WIREFORMATS = EventFactory.class Event.class OverlayNodeSendsRegistration.class RegistryReportsRegistrationStatus.class OverlayNodeSendsDeregistration.class RegistryReportsDeregistrationStatus.class RegistryNodeSendsManifest.class NodeReportsOverlaySetupStatus.class RegistryRequestsTaskInitiate.class OverlayNodeSendsData.class OverlayNodeReportsTaskFinished.class RegistryRequestsTrafficSummary.class OverlayNodeReportsTrafficSummary.class
#package cs455.overlay.routing
ROUTING = RoutingTable.class RoutingEntry.class
#package cs455.overlay.util
UTIL = InteractiveCommandParser.class SummaryAggregator.class


default: all

all: $(WIREFORMATS) $(NODE) $(TRANSPORT) $(EXCEPTION) $(ROUTING) $(UTIL)

#In alias NODE
Node.class:
	@echo "Compiling Node. . ."
	$(JC) -d . $(NODEPATH)Node.java
MessageNode.class:
	@echo "Compiling MessageNode. . . "
	$(JC) -d . $(NODEPATH)MessageNode.java
Registry.class:
	@echo "Compiling Registry. . ."
	$(JC) -d . $(NODEPATH)Registry.java

#In alias TRANSPORT
ServerThread.class:
	@echo "Compiling ServerThread. . ."
	$(JC) -d . $(TRANSPORTPATH)ServerThread.java
Sender.class:
	@echo "Compiling Sender. . ."
	$(JC) -d . $(TRANSPORTPATH)Sender.java
RecieverThread.class:
	@echo "Compiling RecieverThread. . ."
	$(JC) -d . $(TRANSPORTPATH)RecieverThread.java
Connection.class:
	@echo "Compiling Connection. . ."
	$(JC) -d . $(TRANSPORTPATH)Connection.java
ConnectionCache.class:
	@echo "Compiling ConnectionCache. . ."
	$(JC) -d . $(TRANSPORTPATH)ConnectionCache.java
RegisterConnectionCache.class:
	@echo "Compiling RegisterConnectionCache. . ."
	$(JC) -d . $(TRANSPORTPATH)RegisterConnectionCache.java
NodeConnectionCache.class:
	@echo "Compiling NodeConnectionCache. . ."
	$(JC) -d . $(TRANSPORTPATH)NodeConnectionCache.java

#In alias EXCEPTION
ConnectionCacheException.class:
	@echo "Compiling ConnectionClassException. . ."
	$(JC) -d . ./cs455/overlay/exception/ConnectionCacheException.java

#In alias WIREFORAMTS
EventFactory.class:
	@echo "Compiling EventFactory. . ."
	$(JC) -d . $(WIREFORMATSPATH)EventFactory.java
Event.class:
	@echo "Compiling Event. . ."
	$(JC) -d . $(WIREFORMATSPATH)Event.java
OverlayNodeSendsRegistration.class:
	@echo "Compiling OverlayNodeSendsRegistration. . ."
	$(JC) -d . $(WIREFORMATSPATH)OverlayNodeSendsRegistration.java
RegistryReportsRegistrationStatus.class:
	@echo "Compiling RegistryReportsRegistrationStatus. . ."
	$(JC) -d . $(WIREFORMATSPATH)RegistryReportsRegistrationStatus.java
OverlayNodeSendsDeregistration.class:
	@echo "Compiling OverlayNodeSendsDeregistration. . ."
	$(JC) -d . $(WIREFORMATSPATH)OverlayNodeSendsDeregistration.java
RegistryReportsDeregistrationStatus.class:
	@echo "Compiling RegistryReportsDeregistrationStatus. . ."
	$(JC) -d . $(WIREFORMATSPATH)RegistryReportsDeregistrationStatus.java
RegistryNodeSendsManifest.class:
	@echo "Compiling RegistrySendsNodeManifest. . ."
	$(JC) -d . $(WIREFORMATSPATH)RegistrySendsNodeManifest.java
NodeReportsOverlaySetupStatus.class:
	@echo "Compiling NodeReportsOverlaySetupStatus. . ."
	$(JC) -d . $(WIREFORMATSPATH)NodeReportsOverlaySetupStatus.java
RegistryRequestsTaskInitiate.class:
	@echo "Compiling RegistryRequestsTaskInitiate. . ."
	$(JC) -d . $(WIREFORMATSPATH)RegistryRequestsTaskInitiate.java
OverlayNodeSendsData.class:
	@echo "Compiling OverlayNodeSendsData. . ."
	$(JC) -d . $(WIREFORMATSPATH)OverlayNodeSendsData.java
OverlayNodeReportsTaskFinished.class:
	@echo "Compiling OverlayNodeReportsTaskFinished"
	$(JC) -d . $(WIREFORMATSPATH)OverlayNodeReportsTaskFinished.java
RegistryRequestsTrafficSummary.class:
	@echo "Compiling RegistryRequestsTrafficSummary. . ."
	$(JC) -d . $(WIREFORMATSPATH)RegistryRequestsTrafficSummary.java
OverlayNodeReportsTrafficSummary.class:
	@echo "Compiling OverlayNodeReportsTrafficSummary. . ."
	$(JC) -d . $(WIREFORMATSPATH)OverlayNodeReportsTrafficSummary.java

#In alias ROUTING
RoutingTable.class:
	@echo "Compiling RoutingTable. . ."
	$(JC) -d . $(ROUTINGPATH)RoutingTable.java
RoutingEntry.class:
	@echo "Compiling RoutingEntry. . ."
	$(JC) -d . $(ROUTINGPATH)RoutingEntry.java

#In alias UTIL
InteractiveCommandParser.class:
	@echo "Compiling InteractiveCommandParser. . ."
	$(JC) -d . $(UTILPATH)InteractiveCommandParser.java
SummaryAggregator.class:
	@echo "Compiling SummaryAggregator. . ."
	$(JC) -d . $(UTILPATH)SummaryAggregator.java

#let's put all this in a tarball
#Aliases for .java files
JNODE = $(NODEPATH)Node.java $(NODEPATH)MessageNode.java $(NODEPATH)Registry.java
JTRANSPORT = $(TRANSPORTPATH)ServerThread.java $(TRANSPORTPATH)Sender.java $(TRANSPORTPATH)RecieverThread.java $(TRANSPORTPATH)Connection.java $(TRANSPORTPATH)ConnectionCache.java $(TRANSPORTPATH)RegisterConnectionCache.java $(TRANSPORTPATH)NodeConnectionCache.java
JEXCEPTION = ./cs455/overlay/exception/ConnectionCacheException.java
JWIREFORMATS = $(WIREFORMATSPATH)EventFactory.java $(WIREFORMATSPATH)Event.java $(WIREFORMATSPATH)OverlayNodeSendsRegistration.java $(WIREFORMATSPATH)RegistryReportsRegistrationStatus.java $(WIREFORMATSPATH)OverlayNodeSendsDeregistration.java $(WIREFORMATSPATH)RegistryReportsDeregistrationStatus.java $(WIREFORMATSPATH)RegistrySendsNodeManifest.java $(WIREFORMATSPATH)NodeReportsOverlaySetupStatus.java $(WIREFORMATSPATH)RegistryRequestsTaskInitiate.java $(WIREFORMATSPATH)OverlayNodeSendsData.java $(WIREFORMATSPATH)OverlayNodeReportsTaskFinished.java $(WIREFORMATSPATH)RegistryRequestsTrafficSummary.java $(WIREFORMATSPATH)OverlayNodeReportsTrafficSummary.java
JROUTING = $(ROUTINGPATH)RoutingTable.java $(ROUTINGPATH)RoutingEntry.java
JUTIL = $(UTILPATH)InteractiveCommandParser.java $(UTILPATH)SummaryAggregator.java
package:
	tar -cvf Barras_William_ASG1.tar Makefile $(JNODE) $(JTRANSPORT) $(JEXCEPTION) $(JWIREFORMATS) $(JROUTING) $(JUTIL)

#cleans shit up
clean:
	$(RM) ./cs455/overlay/node/Node$(C)
	$(RM) ./cs455/overlay/node/MessageNode$(C)
	$(RM) ./cs455/overlay/node/Registry$(C)
	$(RM) ./cs455/overlay/transport/ServerThread$(C)
	$(RM) ./cs455/overlay/transport/Sender$(C)
	$(RM) ./cs455/overlay/transport/RecieverThread$(C)
	$(RM) ./cs455/overlay/transport/Connection$(C)
	$(RM) ./cs455/overlay/transport/ConnectionCache$(C)
	$(RM) ./cs455/overlay/transport/RegisterConnectionCache$(C)
	$(RM) ./cs455/overlay/transport/NodeConnectionCache$(C)
	$(RM) ./cs455/overlay/exception/ConnectionCacheException$(C)
	$(RM) ./cs455/overlay/exception/NodeCacheException$(C)
	$(RM) ./cs455/overlay/wireformats/EventFactory$(C)
	$(RM) ./cs455/overlay/wireformats/Event$(C)
	$(RM) ./cs455/overlay/wireformats/OverlayNodeSendsRegistration$(C)
	$(RM) ./cs455/overlay/wireformats/RegistryReportsRegistrationStatus$(C)
	$(RM) ./cs455/overlay/wireformats/OverlayNodeSendsDeregistration$(C)
	$(RM) ./cs455/overlay/wireformats/RegistryReportsDeregistrationStatus$(C)
	$(RM) ./cs455/overlay/wireformats/NodeReportsOverlaySetupStatus$(C)
	$(RM) ./cs455/overlay/wireformats/RegistryRequestsTaskInitiate$(C)
	$(RM) ./cs455/overlay/wireformats/OverlayNodeSendsData$(C)
	$(RM) ./cs455/overlay/wireformats/OverlayNodeReportsTaskFinished$(C)
	$(RM) ./cs455/overlay/wireformats/RegistryRequestsTrafficSummary$(C)
	$(RM) ./cs455/overlay/wireformats/OverlayNodeReportsTrafficSummary$(C)
	$(RM) ./cs455/overlay/routing/RoutingEntry$(C)
	$(RM) ./cs455/overlay/routing/RoutingTable$(C)
	$(RM) ./cs455/overlay/util/InterativeCommandParser$(C)
	$(RM) ./cs455/overlay/util/SummaryAggregator$(C)
