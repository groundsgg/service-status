package gg.grounds.application

interface StatusChangePublisher {
    fun motdChanged()
    fun maintenanceChanged()
}
