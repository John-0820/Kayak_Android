package com.kayakpro.erg.network


object Constants {

    //HEADER KEY
//----------------------------------------------------------------
    const val TOKEN_KEY = "Bearer "
    const val LOGIN = "api/authenticate"
    const val TOKEN = "token"
    const val POST_TYPE = ""
    const val REGISTER = "api/register"
    const val FORGOTPSWD = "api/account/reset-password/init"
    const val FORGOTPSWDFINISH = "api/account/reset-password/finish"
    const val KEY = "key"
    const val EMAIL = "email"
    const val NewPswd = "newPassword"
    const val GET_TRAINING = "api/program-trainings"
    const val GET_TRAINING_Data = "api/program-trainings/"
    const val GET_TRAININ_DATA = "/api/program-trainings/data"
    const val DELETE_TRAINING_HISTORY = "api/training-histories"
    const val DELETE_TRAINING = "api/program-trainings"
    const val GET_FITFILE = "api/training-histories/"
    const val GET_TRAINING_HISTORY = "api/training-histories"
    const val CREATE_TRAINING_HISTORY = "api/training-histories"

    const val CREATE_AVATAR_TRAINING = "api/avatar-trainings"
    const val CREATE_PROGRAM_TRAINING = "api/program-trainings"
    const val UPDATE_PROGRAM_TRAINING = "api/program-trainings"
    var BPM = 0
    var STROKE = 0
    var DISTANCE = 0
    var WATTS = 0

}