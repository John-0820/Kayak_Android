package com.kayakpro.erg

import com.garmin.fit.*
import java.io.File
import java.util.Date
import android.content.Context

class FitEncoder(private val context: Context) {

    private val fitFile: File
    private val fileEncoder: FileEncoder
    private var startTimeMillis: Long = 0L

    init {
        val dir = java.io.File(context.getExternalFilesDir(null), "activities")
        if (!dir.exists()) dir.mkdirs()

        fitFile = java.io.File(
            dir,
            "kayak_${System.currentTimeMillis()}.fit"
        )

        fileEncoder = FileEncoder(fitFile, Fit.ProtocolVersion.V2_0)
    }

    fun startSession(startTimeMillis: Long) {

        this.startTimeMillis = startTimeMillis

        val fileId = FileIdMesg().apply {
            type = com.garmin.fit.File.ACTIVITY
            manufacturer = Manufacturer.DEVELOPMENT
            setTimeCreated(DateTime(Date(startTimeMillis)))
        }
        fileEncoder.write(fileId)

        val sportMesg = SportMesg().apply {
            sport = Sport.KAYAKING
        }
        fileEncoder.write(sportMesg)

        val startEvent = EventMesg().apply {
            event = Event.TIMER
            eventType = EventType.START
            setTimestamp(DateTime(Date(startTimeMillis)))
        }
        fileEncoder.write(startEvent)
    }

    fun addRecord(
        timestampMillis: Long,
        distanceMeters: Float,
        speedMetersPerSecond: Float,
        powerWatts: Int,
        cadence: Int,
        heartRate: Int?
    ) {
        val record = RecordMesg().apply {
            setTimestamp(DateTime(Date(timestampMillis)))
            distance = distanceMeters
            speed = speedMetersPerSecond
            setPower(powerWatts)
            setCadence(cadence.toShort())
            heartRate?.let { setHeartRate(it.toShort()) }
        }
        fileEncoder.write(record)
    }

    fun endSession(
        totalDistanceMeters: Float,
        totalElapsedTimeSec: Float
    ) {
        val stopTime = System.currentTimeMillis()

        val stopEvent = EventMesg().apply {
            event = Event.TIMER
            eventType = EventType.STOP
            setTimestamp(DateTime(Date(stopTime)))
        }
        fileEncoder.write(stopEvent)

        val session = SessionMesg().apply {
            sport = Sport.KAYAKING
            totalDistance = totalDistanceMeters
            totalElapsedTime = totalElapsedTimeSec
            setTimestamp(DateTime(Date(stopTime)))
            setStartTime(DateTime(Date(startTimeMillis)))

            totalTimerTime = totalElapsedTimeSec
        }
        fileEncoder.write(session)

        val activity = ActivityMesg().apply {
            timestamp = DateTime(Date(stopTime))
            totalTimerTime = totalElapsedTimeSec
            numSessions = 1
        }
        fileEncoder.write(activity)

        fileEncoder.close()
    }

    fun getFitFile(): File = fitFile
}
