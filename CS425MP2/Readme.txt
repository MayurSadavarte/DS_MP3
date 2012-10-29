To run this program. we must first run Contact on one machine, then run Machine on other machines.

To run Contact, use the following command:
	java -jar 425mp2Contact.jar
	
To run Machine, use the following command:
	java -jar 425mp2.jar $(contact's IP address)
	
The log file on each machine is:
	$(machine's IP address).log
	
The log file of the contact is:
	Contact.log