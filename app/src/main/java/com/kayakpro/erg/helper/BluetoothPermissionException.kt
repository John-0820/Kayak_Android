package com.kayakpro.erg.helper

class BluetoothPermissionException : RuntimeException {

    constructor() : super("Bluetooth permission denied.")

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super("Bluetooth permission denied.", cause)
}