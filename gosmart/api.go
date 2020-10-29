// This file is part of gosmart, a set of libraries to communicate with
// the Samsumg SmartThings API using Go (golang).
//
// http://github.com/marcopaganini/gosmart
// (C) 2016 by Marco Paganini <paganini@paganini.net>

package gosmart

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
)

type getSensorsResult struct {
	Descriptions map[string]string `json:"descriptions"`
	Sensors map[string]sensorInfo `json:"sensors"`
}

type sensorInfo struct {
	Name        string `json:"name"`
	DisplayName string `json:"displayName"`
	Attributes map[string]float64 `json:"attributes"`
}

type Attribute struct {
	Name 		string
	Description	string
	Value		float64
}

type Sensor struct {
	ID 			string
	Name        string
	DisplayName string
	Attributes	[]Attribute
}

// GetSensors returns the list of devices from smartthings using
// the specified http.client and endpoint URI.
func GetSensors(client *http.Client, endpoint string) ([]Sensor, error) {
	contents, err := issueCommand(client, endpoint, "/sensors")
	if err != nil {
		return nil, err
	}

	ret := getSensorsResult{}
	if err := json.Unmarshal(contents, &ret); err != nil {
		return nil, err
	}

	var sensors []Sensor
	for id, sensorInfo := range ret.Sensors {
		var attributes []Attribute
		for name, value := range sensorInfo.Attributes {
			attribute := Attribute{
				Name:        name,
				Description: ret.Descriptions[name],
				Value:       value,
			}
			attributes = append(attributes, attribute)
		}
		sensor := Sensor{
			ID:          id,
			Name:        sensorInfo.Name,
			DisplayName: sensorInfo.DisplayName,
			Attributes:  attributes,
		}
		sensors = append(sensors, sensor)
	}
	return sensors, nil
}

// issueCommand sends a given command to an URI and returns the contents
func issueCommand(client *http.Client, endpoint string, cmd string) ([]byte, error) {
	uri := endpoint + cmd
	resp, err := client.Get(uri)
	if err != nil {
		return nil, err
	}
	contents, err := ioutil.ReadAll(resp.Body)
	resp.Body.Close()
	if err != nil {
		return nil, err
	}
	return contents, nil
}
