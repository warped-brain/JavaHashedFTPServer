# JavaHashedFTPServer
## Overview
This project implements an FTP server using SSL for secure communication and SHA-256 to verify the integrity of the transmitted files. The server allows clients to connect, list available files, and download files with their integrity verified using SHA-256.

## Features
SSL Encryption: Secure communication between the client and the server.
File Integrity Verification: Uses SHA-256 hashing to ensure files are transmitted without tampering.
Directory Listing: Lists files in the shared directory.
File Transfer: Supports file retrieval via the RETR command.
Graceful Shutdown: Allows the client to disconnect using the QUIT command.
## Requirements
Java Development Kit (JDK) 8 or higher
A valid Java KeyStore (JKS) file
A directory with files to share
## Setup
1. Generate KeyStore (if you don't have one)
You can generate a KeyStore using the keytool command that comes with the JDK.
```
keytool -genkeypair -alias serverkey -keyalg RSA -keystore serverkeystore.jks -keysize 2048
```
2. Configure the Server
Modify the following variables in the FTPSServer code:

port: The port number you want the server to listen on (default is 8080).
keystorePath: The path to your KeyStore file.
password: The password for your KeyStore.

3. Compile and Run

## Usage
Start the Server:
Run the FTPSServer class. The server will prompt you to enter the directory to share.

Client Connection:
Use an SSL-capable client to connect to the server. For example, you can use openssl s_client or a custom Java client.

List Files:
The server will automatically send the list of files in the shared directory.

Retrieve a File:
Use the RETR <filename> command to download a file. The server will send the file along with its SHA-256 hash.

Quit:
Use the QUIT command to disconnect from the server.
