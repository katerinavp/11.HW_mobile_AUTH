package org.example.exception

class RegistrationException(message: String? = "User already exists") : Exception(message)