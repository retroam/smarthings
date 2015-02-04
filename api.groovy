/**
 *  Sensable
 *  Copyright 2015 Sensable
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Sensable",
    namespace: "",
    author: "Sensable",
    description: "API for Shield app",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

/* --- setup section --- */
preferences {
	section("Allow Shield to Control & Access These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
        input "alarms", "capability.alarm", title: "Which Alarms?", multiple: true, required: false
        input "contacts", "capability.contactSensor", title: "Which Contacts?", multiple: true, required: false
       // input "carbon","capability.carbonMonoxide", title: "Which CO detectors?", multiple: true, required: false
        //input "doors", "capability.doorControl", title: "Which Doors?", multiple: true, required: false
        input "motion", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
        input "presence", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
        //input "smoke", "capability.smokeDetector", title: "Which Smoke Detectors?", multiple: true, required: false
        input "temperature", "capability.temperatureMeasurement", title: "Which Temperature Sensors?", multiple: true, required:false

	}
}

/* --- API mapping--- */
mappings {
	path("/:type") {
		action: [
			GET: "api_list",
            PUT: "api_update"
		]
	}
	path("/:type/:id") {
		action: [
			GET: "api_get",
			PUT: "api_put"
		]
	}     
}
/*
 * Called when app is installed
 */
def installed() {
	event_subscribe()
    }

/*
 * Called when user changes preferences
 */
def updated() {
    log.debug "updated"
    unsubscribe()

	}

/* --- event section --- */
def event_subscribe()
{  
    log.debug "subscribed"
    subscribe(switches, "switch", on_event)
    subscribe(alarms, "alarm", on_event)
    subscribe(locks, "lock", on_event)
    subscribe(contacts, "contactSensor", on_event)
    //subscribe(carbon, "carbonMonoxide", "on_event")
    //subscribe(doors, "doorControl", "on_event")
    subscribe(motion, "motionSensor", on_event)
    //subscribe(smoke, "smokeDetector", "on_event")
    subscribe(temperature, "temperature", on_event)
    subscribe(presence, "presenceSensor", on_event)

}

def on_event(evt)
{
    log.debug "on_event"
    def dt = device_and_type_for_event(evt)
    if (!dt) {
        log.debug "on_event deviceId=${evt.deviceId} not found?"
        return;
    }
    
    def jd = device_to_json(dt.device, dt.type)
    log.debug "on_event deviceId=${jd}"

    send(dt.device, dt.type, jd)

}


/* --- API parsing --- */
def api_list(){
log.debug "api_list ${params.type}"
    devices_for_type(params.type).collect{
       device(it, params.type)
   }
}


def api_update(){
  log.debug "put request all devices"
  def devices = devices_for_type(params.type)
  if (!devices) {
        httpError(404, "Devices not found")
    } else {
        devices_command(devices)
    }
}

def api_put(){
    log.debug "put request"
    def devices = devices_for_type(params.type)
    def device = devices.find { it.id == params.id }
    if (!device) {
        httpError(404, "Device not found")
    } else {
        device_command(device)
    }
}

def api_get(){
    log.debug "get request"
    def devices = devices_for_type(params.type)
    def device = devices.find { it.id == params.id }
    if (!device) {
        httpError(404, "Device not found")
    } else {
        def s = device.currentState(params.type)
		[id: device.id, label: device.displayName, name: device.displayName, state: s]
    }
}

void api_update(){
    log.debug "api_update"
    do_update(devices_for_type(params.type), params.type)
}

def deviceHandler(evt) {
}

/*
 *  Devices and Types Dictionary
 */
def dtd(){
    log.debug "call dtd"
    [ 
        switch: switches,
        motion: motion, 
        contact: contacts,
        presence: presence,
        lock: locks,
        //carbon: carbon,
        //door: doors,
        //smoke: smoke,
        temperature: temperature,
        alarm: alarms

    ]
}

def devices_for_type(type) {
    log.debug "devices_for_type${ dtd()[type]}"
    dtd()[type]
}

def device_and_type_for_event(evt)
{  
    log.debug "evt ${evt}"
    def dtd = dtd()
    
    for (dt in dtd()) {
        if (dt.key != evt.name) {
        	continue
        }
        
        def devices = dt.value
        for (device in devices) {
            if (device.id == evt.deviceId) {
                return [ device: device, type: dt.key ]
            }
        }
    }
}

/*
 *  Device Commands
 */
 private void devices_command(devices) {
 		def command = request.JSON?.command
		if (command) {
			devices."$command"()
		}
    
}

private void device_command(devices) {
	log.debug "update, request: ${request.JSON}, params: ${params}, devices: $devices.id"
	def command = request.JSON?.command
	if (command) {
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
			device."$command"()
		}
	}
}

private device_to_json(device, type) {
	log.debug "device_to_json ${type}"
	device(it, type)
}

private device(it, name) {
	if (it) {
		def s = it.currentState(name)
		[id: it.id, label: it.displayName, name: it.displayName, state: s]
    }
}

/*
 *  Communication
 */

def settings()
{
    [ 
		api_username: "",
        api_key: ""
    ]
}

def send(device, device_type, deviced) {
	def settings = settings()
    
    if (!settings.api_username || !settings.api_key) {
        return
    }

    log.debug "send() called";

    def now = Calendar.instance
    def date = now.time
    def millis = date.time
    def sequence = millis
    def isodatetime = deviced?.value?.timestamp
    
    def digest = "${settings.api_key}/${settings.api_username}/${isodatetime}/${sequence}".toString();
    def hash = digest.encodeAsMD5();
    
    def topic = "st/${device_type}/${deviced.id}".toString()
    
    def uri = "http://127.0.0.1:5000/results"
    def headers = [:]
    def body = [
        "topic": topic,
        "payloadd": deviced?.value,
        "timestamp": isodatetime,
        "sequence": sequence,
        "signed": hash,
        "username": settings.api_username
    ]

    def params = [
        uri: uri,
        headers: headers,
        body: body
    ]

    log.debug "send : params=${params}"
    httpPutJson(params) { log.debug "send: response=${response}" }
}



