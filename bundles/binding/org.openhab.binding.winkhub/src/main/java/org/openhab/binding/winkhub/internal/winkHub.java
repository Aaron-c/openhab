package org.openhab.binding.winkhub.internal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class winkHub {
	public void modifyLightVal(String Light_name, double val) {
		Device d = all_devices.get(Light_name);
		if (d==null) {
			System.out.println(Light_name + "not found");
		} else {
			d.ChangeDimmer(val);
		}
	}
	public Map<String, Device> all_devices; 
	public class Device {
		public Device(JSONObject js, winkHub hub, String type) {
			name = (String) js.get("name");
			device_id = (String) js.get(type + "_id");
			type_ = type +"s";//(String)js.get("type");
			state_ = js;
			hub_ = hub;
		}
		String name;
		String type_;
		String device_id;
		JSONObject state_;
		winkHub hub_;
		@SuppressWarnings("unchecked")
		public void ChangeDimmer(double new_val) {
			JSONObject desired_state = (JSONObject) state_.get("desired_state");
			double old_val = (Double)desired_state.get("brightness"); 
			//double old_val = Double.parseDouble(old_val_str);
			new_val += old_val;
			new_val = Math.min(new_val, 1.0);
			new_val = Math.max(new_val, 0.0);
			//
			
			desired_state.put("brightness", new_val);
			if (new_val == 0) {
				desired_state.put("powered", false);
			} else {
				desired_state.put("powered", true);
			}
			hub_.UpdateDeviceState(this);
			//example of the code desired_state":{"brightness":1.0,"powered":true,"brightness_updated_at":1.42914667345876E9,"powered_updated_at":1.429146658122461E9}
		}
	}

	public class OAuthData {
		public String username = "noraac@hotmail.com";
		public String password = "noraac";
		public String client_id = "quirky_wink_android_app";
		public String client_secret = "e749124ad386a5a35c0ab554a4f2c045";
		public String access_token  =null;
		public String refresh_token= null;
	}
	
	public OAuthData data = new OAuthData();
	public String page_ = "https://winkapi.quirky.com";
	//public String page_ ="https://private-anon-321c9a489-wink.apiary-mock.com";
	public winkHub() {
		
	}
	
	
	public boolean SignIn() {
		String payload_st = "{\n    \"client_id\": \"" + data.client_id;
		payload_st +=       "\",\n    \"client_secret\": \"" + data.client_secret;
		payload_st +=       "\",\n    \"username\": \"" + data.username;
		payload_st +=       "\",\n    \"password\": \"" +data.password;
		payload_st +=       "\",\n    \"grant_type\": \"password\"\n}";
		//System.out.println("status: " + payload_st);
		ClientResponse response = post("/oauth2/token", payload_st);
		JSONParser parser=new JSONParser();
		try {
			JSONObject json_response = (JSONObject) parser.parse(response.getEntity(String.class));
			data.access_token = (String) json_response.get("access_token");
			data.refresh_token =  (String) json_response.get("refresh_token");
			System.out.println("status: " + response.getStatus());
			System.out.println("headers: " + response.getHeaders());
		//	logger.debug("body:" + json_response);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		response.close();
		return true;
	}
	
	public Client client = Client.create();

	Map<String, WebResource> cachedResources = new HashMap<String, WebResource>();
	// I found that without this cache, a complete renegotiation of tcp/tls was happening with every command
	// so now I cache the resources.
	public WebResource GetWebResource(String path) {
		if (cachedResources.containsKey(path)) {
			return cachedResources.get(path);
		}
		WebResource a = client.resource(path);
		cachedResources.put(path,  a);
		return a;
	}
	
	public void RefreshToken() {
		String payload_st = "{\n    \"client_id\": \"" + data.client_id;
		payload_st +=       "\",\n    \"client_secret\": \"" + data.client_secret;
		payload_st +=       "\",\n    \"grant_type\": \"refresh_token\"";
		payload_st +=       "\"refresh_token\": \"" + data.refresh_token + "\"\n}";

//		Entity payload = Entity.json(payload_st);

		WebResource webResource = GetWebResource(page_ + "/oauth2/token");
		 
		ClientResponse response = webResource.accept("application/json")
		                   .post(ClientResponse.class, payload_st);
	/*	Response response = client.target(page_)
		  .path("/oauth2/token")
		  .request(MediaType.APPLICATION_JSON_TYPE)
		  .post(payload);
*/
		JSONParser parser=new JSONParser();
		try {
			JSONObject json_response = (JSONObject) parser.parse(response.getEntity(String.class));
			data.access_token = (String) json_response.get("access_token");
			data.refresh_token =  (String) json_response.get("refresh_token");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.close();
	}
	
	public void CheckAuthorization() {
		if (data.access_token == null && data.refresh_token == null) {
			SignIn();
		} 
		if (data.access_token == null) {
			RefreshToken();
		}
	}
	
	public ClientResponse get(String path) {
		CheckAuthorization();
		WebResource webResource = GetWebResource(page_ + path);
		
		ClientResponse response = webResource
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.type(MediaType.APPLICATION_JSON_TYPE)
					.header("Authorization", "Bearer " + data.access_token)
                    .get(ClientResponse.class);
 
		if (response.getStatus() != 200) {
		   throw new RuntimeException("Failed : HTTP error code : "
			+ response.getStatus());
		}
		/*Client client = ClientBuilder.newClient();
		
		return client.target(page_)
		  .path(path)
		  .request(MediaType.TEXT_PLAIN_TYPE)
		  .header("Authorization", "Bearer " + data.access_token)
		  .get();
		  */
		return response;
	}

	
	@SuppressWarnings("rawtypes")
	public Map<String, Device> GetAllDevices(String type) {
		ClientResponse response = get("/users/me/" + type + "s"); 
		Map<String, Device> devices = new HashMap<String, Device>();
		if (response.getStatus() ==200) {
			JSONParser parser=new JSONParser();
			try {
				JSONObject json_response = (JSONObject) parser.parse(response.getEntity(String.class)); 
				JSONArray jdevices = (JSONArray) json_response.get("data");
				Iterator i = jdevices.iterator();
		        while (i.hasNext()) {
		        	JSONObject d = (JSONObject) i.next();
		        	Device device = new Device(d, this, type);
		        	devices.put(device.name , device);
//		            System.out.println(d);
		            // Here I try to take the title element from my slide but it doesn't work!
		            // System.out.println(title);
		        }
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return devices;
	}
	
	public Map<String, Device> GetAllDevices() {
		return GetAllDevices("light_bulb");
	}

	public ClientResponse post(String path, String payload) {
		WebResource webResource = GetWebResource(page_ + path);
		 
//		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON_TYPE)
//		                   .post(ClientResponse.class, payload);
		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, payload);

//		System.out.println("status: " + response.getStatus());
//		System.out.println("headers: " + response.getHeaders());
//		System.out.println("body:" + response.toString());

		return response;
	}
	
	
	public ClientResponse put(String path, String payload) {
		WebResource webResource = GetWebResource(page_ + path);
		
		ClientResponse response = webResource
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.header("Authorization", "Bearer " + data.access_token)
				.put(ClientResponse.class, payload);

		if (response.getStatus() != 200) {
			   throw new RuntimeException("Failed : HTTP error code : "
				+ response.getStatus());
		}
		
		
		return response;
		
/*		Client client = ClientBuilder.newClient();
		//System.out.println(path);
		Response response = client.target(page_)
		  .path(path)
		  .request(MediaType.APPLICATION_JSON_TYPE)
		  .header("Authorization", "Bearer " + data.access_token)
		  .put(payload);
		
		//System.out.println("status: " + response.getStatus());
		//System.out.println("headers: " + response.getHeaders());
		//System.out.println("body:" + response.readEntity(String.class));
		return response;*/
	}
	
	public void UpdateDeviceState(Device d) {
		String path = "/" + d.type_ + "/" + d.device_id;
		ClientResponse response = put(path, d.state_.toJSONString());
		//System.out.println(path);

		//System.out.println(d.state_.toString());
	}
}

