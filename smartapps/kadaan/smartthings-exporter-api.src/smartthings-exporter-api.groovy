/**
 *  Copyright 2020 Joel Baranick
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
 *  Smartthings Exporter API
 *
 *  Author: kadaan
 */

import groovy.transform.EqualsAndHashCode

definition(
  name: "Smartthings Exporter API",
  namespace: "kadaan",
  author: "Joel Baranick",
  description: "API used by Smartthings_exporter to read sensor data.",
  category: "My Apps",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  oauth: [displayName: "Smartthings_exporter API", displayLink: ""])

preferences {
  section() {
    paragraph "Select the sensors you want the API to have access to."
  }
  section() {
    input "sensors", "capability.sensor", multiple: true, title: "Which sensors?", required: false
    input "actuators", "capability.actuator", multiple: true, title: "Which actuators?", required: false
    input "switches", "capability.switch", multiple: true, title: "Which switches?", required: false
  }
}

def installed() {
  initialize()
}

def updated() {
  unsubscribe()
  initialize()
}

def initialize() {
}

mappings {
  path("/sensors") {
    action: [
      GET: "listSensors"
    ]
  }
}

def Map getAttributeMappings() {
    return [
    	"Alarm" : [
            "alarmState" : [
                name: "alarm_cleared",
                description: "0 if the alarm is clear.",
                conversion: this.&valueClear
            ]
        ],
        "Battery" : [
            "battery": [
                name: "battery_percentage",
                description: "Percentage of battery remaining.",
                conversion: this.&valueFloat
            ]
        ],
        "Carbon Monoxide Detector" : [
            "carbonMonoxide" : [
                name: "carbon_monoxide_detected",
                description: "1 if the carbon monoxide is detected.",
                conversion: this.&valueClear
            ]
        ],
        "Color Temperature" : [
            "colorTemperature" : [
                name: "color_temperature_kelvins",
                description: "Light color temperature.",
                conversion: this.&valueFloat
            ]
        ],
        "Contact Sensor" : [
            "contact" : [
                name: "contact_closed",
                description: "1 if the contact is closed.",
                conversion: this.&valueOpenClosed
            ]
        ],
        "Energy Meter" : [
            "energy" : [
                name: "energy_usage_joules",
                description: "Energy usage in joules.",
                conversion: this.&valueJoules
            ]
        ],
        "Relative Humidity Measurement" : [
            "humidity" : [
                name: "relative_humidity_percentage",
                description: "Current relative humidity percentage.",
                conversion: this.&valueFloat
            ]
        ],
        "Color Control" : [
            "hue" : [
                name: "color_hue_percentage",
                description: "Light color hue percentage.",
                conversion: this.&valueFloat
            ],
            "saturation" : [
                name: "color_saturation_percentage",
                description: "Light color saturation percentage.",
                conversion: this.&valueFloat
            ]
        ],
        "Illuminance Measurement" : [
            "illuminance" : [
                name: "illuminance_lux",
                description: "Light illuminance in lux.",
                conversion: this.&valueFloat
            ]
        ],
        "Switch Level" : [
            "level" : [
                name: "level",
                description: "Level as a percentage.",
                conversion: this.&valueFloat
            ]
        ],
        "Motion Sensor" : [
            "motion" : [
                name: "motion_detected",
                description: "1 if motion is detected.",
                conversion: this.&valueInactiveActive
            ]
        ],
        "Power Meter" : [
            "power" : [
                name: "power_usage_watts",
                description: "Current power usage in watts.",
                conversion: this.&valueFloat
            ]
        ],
        "Presence Sensor" : [
            "presence" : [
                name: "presence_detected",
                description: "1 if presence is detected.",
                conversion: this.&valueAbsentPresent
            ]
        ],
        "Smoke Detector" : [
            "smoke" : [
                name: "smoke_detected",
                description: "1 if smoke is detected.",
                conversion: this.&valueClear
            ]
        ],
        "Switch" : [
            "switch" : [
                name: "switch_enabled",
                description: "1 if the switch is on.",
                conversion: this.&valueOnOff
            ]
        ],
        "Tamper Alert" : [
            "tamper" : [
                name: "tamper_sensor_clear",
                description: "0 if the tamper sensor is clear.",
                conversion: this.&valueClear
            ]
        ],
        "Temperature Measurement" : [
            "temperature" : [
                name: "temperature_fahrenheit",
                description: "Temperature in fahrenheit.",
                conversion: this.&valueFloat
            ]
        ],
        "Voltage Measurement" : [
            "voltage" : [
                name: "voltage_volts",
                description: "Energy voltage in Volts.",
                conversion: this.&valueFloat
            ]
        ]
    ]
}

def listSensors() {
    def start = new Date()
    def attributeMappings = getAttributeMappings()
    def descriptions = [:]
    def metrics = [:]
    def devices = [:]
    actuators.each {
      devices[it.id] = it
    }
    sensors.each {
      if(!devices.containsKey(it.id)) {
        devices[it.id] = it
      }
    }
    switches.each {
      if(!devices.containsKey(it.id)) {
        devices[it.id] = it
      }
    }
    devices.each {
        def sensor = it.value
        def metricDescriptions = [:]
        def metric = [:]
        def metricAttributes = [:]
        def supportedCapabilities = sensor.capabilities
        supportedCapabilities.each {
        	if (attributeMappings.containsKey(it.name)) {
	            def attributeMapping = attributeMappings.get(it.name)
            	attributeMapping.each {
                    def currentValue = sensor.currentValue(it.key)
                    if (currentValue) {
                        metricDescriptions[it.value.name] = it.value.description
                        metricAttributes[it.value.name] = it.value.conversion(currentValue)
                    }
                }
            }
        }
        if (metricAttributes.size() > 0) {
            ["name", "displayName"].each {
                metric[it] = sensor."$it"
            }
            metric.attributes = metricAttributes
            descriptions = descriptions << metricDescriptions
            metrics[sensor.id] = metric
        }
    }
    def end = new Date()
    log.debug("${end.getTime() - start.getTime()}")
    [descriptions: descriptions, sensors: metrics]
}

private valueClear(value) {
    if (!value?.trim() || value?.trim().toString() == "clear") {
        return 0.0
    }
    return 1.0
}

private valueFloat(value) {
    if (!value) {
        return 0.0
    }
    try {
        float f = Float.valueOf(value).floatValue();
        return f
    } catch (NumberFormatException nfe) {
        return 0.0
    }
}

private valueOneOf(value, options) {
    if (!value?.trim()) {
        return 0.0
    }
    if (value?.trim() == options[0]) {
        return 0.0
    }
    if (value?.trim() == options[1]) {
        return 1.0
    }
    return 0.0
}

private valueOnOff(value) {
    return valueOneOf(value, ["off", "on"])
}

private valueOpenClosed(value) {
    return valueOneOf(value, ["open", "closed"])
}

private valueAbsentPresent(value) {
    return valueOneOf(value, ["not present", "present"])
}

private valueInactiveActive(value) {
    return valueOneOf(value, ["inactive", "active"])
}

private valueJoules(value) {
    return valueFloat(value) * 3600000
}