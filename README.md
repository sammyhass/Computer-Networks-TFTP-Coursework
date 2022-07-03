#  Computer Networks Coursework

This repo contains the coursework for the Computer Networks module which I took as part of my second year studying computer science at Sussex Univeristy.

The task for this coursework was to implement the TFTP Protocol as defined in [RFC 1350](https://datatracker.ietf.org/doc/html/rfc1350).

We were asked to implement both the client and server of the protocol and in additional we had to create a UDP version of the server and client as well as a TCP version of the server and client.

This coursework was particularly interesting as it was a great opportunity to learn about the protocol and how it works, in addition it was fantastic to get some experience reading and implementing protocols from an RFC, something I had never done before and gave me an chance to see how real world protocols are standardised using RFCs.

For this coursework, my final mark was 94/100.

## Structure

* `Report.pdf` - The report I produced for this coursework.
* `TFTP-UDP-Server` - UDP implementation of the TFTP server
* `TFTP-UDP-Client` - UDP implementation of the TFTP client - a simple command line client
* `TFTP-TCP-Server` - TCP implementation of the TFTP server
* `TFTP-TCP-Client` - TCP implementation of the TFTP client - also a simple command line client

In order to properly test the servers work, I used the `tftp` command line client which is available on Unix systems.
