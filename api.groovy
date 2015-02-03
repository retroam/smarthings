/**
 *  Sensable
 *
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


preferences {
	section("Allow Shield to Control These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
        input "alarms", "capability.alarm", title: "Which Alarms?", multiple: true, required: false
        input "contacts", "capability.contactSensor", title: "Which Contacts?", multiple: true, required: false
	}
}

mappings {
	path("/switches") {
		action: [
			GET: "listSwitches",
			PUT: "updateSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch",
			PUT: "updateSwitch"
		]
	}
    path("/locks") {
		action: [
			GET: "listLocks",
			PUT: "updateLocks"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock",
			PUT: "updateLock"
		]
	}
    path("/alarms") {
		action: [
			GET: "listAlarms",
			PUT: "updateAlarms"
		]
	}
	path("/alarms/:id") {
		action: [
			GET: "showAlarm",
			PUT: "updateAlarm"
		]
	}
    
     path("/contacts") {
		action: [
			GET: "listContacts",
			PUT: "updateContacts"
		]
	}
	path("/contacts/:id") {
		action: [
			GET: "showContact",
			PUT: "updateContact"
		]
	}
	

      
}

def installed() {}

def updated() {}

def listSwitches() {
	switches.collect { device(it,"switch") }
}
void updateSwitches() {
	updateAll(switches)
}
def showSwitch() {
	show(switches, "switch")
}
void updateSwitch() {
	update(switches)
}

def listLocks() {
	locks.collect { device(it, "lock") }
}
void updateLocks() {
	updateAll(locks)
}
def showLock() {
	show(locks, "lock")
}
void updateLock() {
	update(locks)
}


def listAlarms() {
	alarms.collect { device(it, "alarm") }
}
void updateAlarms() {
	updateAll(alarms)
}
def showAlarm() {
	show(alarms, "alarm")
}
void updateAlarm() {
	update(alarms)
}

def listContacts() {
	contacts.collect { device(it, "contactSensor") }
}
void updateContacts() {
	updateAll(contacts)
}
def showContact() {
	show(contacts, "contactSensor")
}
void updateContact() {
	update(contacts)
}

private void updateAll(devices) {
	def command = request.JSON?.command
	if (command) {
		devices."$command"()
	}
}

private void update(devices) {
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

private show(devices, name) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def s = device.currentState(name)
		[id: device.id, label: device.displayName, name: device.displayName, state: s]
	}
}

private device(it, name) {
	if (it) {
		def s = it.currentState(name)
		[id: it.id, label: it.displayName, name: it.displayName, state: s]
    }
}