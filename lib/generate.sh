#!/bin/bash
java -jar avro-tools-1.7.7.jar compile protocol ../protocols/avpr/server/Server.avpr ../protocols/
java -jar avro-tools-1.7.7.jar compile protocol ../protocols/avpr/SmartFridge/SmartFridge.avpr ../protocols/
java -jar avro-tools-1.7.7.jar compile protocol ../protocols/avpr/user/user.avpr ../protocols/
java -jar avro-tools-1.7.7.jar compile protocol ../protocols/avpr/Electable/Electable.avpr ../protocols/

