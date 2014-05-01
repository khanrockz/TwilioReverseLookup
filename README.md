TwilioReverseLookup
===================
This application is written in Play 2.1 framework in Scala using Twilio API's. It is a reverse directory lookup application that when given a US telephone number can retrive metadata associated with it and play it to the user. here is how it works:

1) User calls a Twilio Number
2) Twilio number issues a GET call on the server where it is redirected and ends up on Play application
3) Twilio is sent back an XML to gather the digits.
4) Twilio gathers and responds with digits captured.
5) Application makes another restful call to retrieve metadata assocaited with number.
6) Application uses Twilio to play back name, age and location of the number given.
7) If not found, it will play an error message.
