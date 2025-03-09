## Overview
* This project is dedicated to leverage DES encryption algorithm for sending  files across computers securely.
## Features
* ##### Encrypt and decrypt files (of text and image format) using DES algorithm
* ##### Sending files across computers using Socket TCP
## Project structure

    src/
        main/
            java/
                com/encryption/
                    server/
                        Server.java         # Server main class
                        ServerController.java  # UI controller
                        ClientHandler.java  # Handles each client connection
                    client/
                        Client.java         # Client main class
                    crypto/
                        DES.java     # Encryption/decryption logic
                    network/
                        NetworkUtil.java    # Socket communication utilities
                    utils/
                        FileUtil.java       # File I/O utilities
            resources/
                server.fxml             # Server UI layout
                client.fxml             # Server UI layout
                styles.css              # UI styling
                config.properties       # Configuration (port, key)
        test/
            java/
                com/encryption/test/
                    CryptoTest.java     # Unit tests for crypto
                    NetworkTest.java    # Unit tests for network
        pom.xml                     # Maven build file
    README.md                       # Project documentation

# Run application

# Reach out