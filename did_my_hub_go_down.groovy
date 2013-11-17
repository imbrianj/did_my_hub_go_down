/**
 *  Did My Hub Go Down?
 *
 *  Author: brian@bevey.org
 *  Date: 9/19/13
 *
 *  Attempts to check a device on your hub every half hour to determine
 *  if it's still reporting data.  If, during a defined interval, it will let
 *  you know that it may be down.  Currently relies on temperature sensors,
 *  as they report in regular intervals.
 */

preferences {
  section("Things to check?"){
    input "tempSensor", "capability.temperatureMeasurement"
  }

  section("False alarm threshold?") {
    input "falseAlarmThreshold", "decimal", title: "Number of minutes (defaults to 60)", required: false
  }

  section("Notifications") {
    input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required: false
    input "phone", "phone", title: "Send a Text Message?", required: false
  }
}

def installed() {
  schedule("0 0,30 * * * ?", scheduleCheck) // Check at top and half-past of every hour
}

def updated() {
  unsubscribe()
  schedule("0 0,30 * * * ?", scheduleCheck)
}

def scheduleCheck() {
  def deltaMinutes = falseAlarmThreshold ?: 60
  def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())

  if(!tempSensor.eventsSince(timeAgo)) {
    send("It looks like your SmartThings hub may be down")
  }

  else {
    log.info("Recent activity - still alive")
  }
}

private send(msg) {
  if(sendPushMessage != "No") {
    log.debug("Sending push message")
    sendPush(msg)
  }

  if(phone) {
    log.debug("Sending text message")
    sendSms(phone, msg)
  }

  log.debug(msg)
}