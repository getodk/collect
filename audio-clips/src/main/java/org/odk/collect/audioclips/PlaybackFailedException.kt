package org.odk.collect.audioclips

data class PlaybackFailedException(val uRI: String, val exceptionMsg: Int) : Exception()
