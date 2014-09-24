RadioReminder
=============


This is a service that watches for certain events and responds by executing a specified action. The response can be delayed by waiting for some specified time, or some other action.

For instance, it can watch for 
"turning wifi off" and respond with "turning wifi on" in "30 mins"

or

"turning wifi off" and respond with "turning wifi on" when device is "plugged in"

or

"turning bluetooth on" and respond with "turn bluetooth off" in "60 mins"


The core functionality is in place so, added new types of events shouldn't be difficult.