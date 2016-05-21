package yellowPage;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.*;

public class YellowPageService {

	public static void registerService(String service) throws Exception {
		InetAddress IP = InetAddress.getLocalHost();
		String ipaddress = IP.getHostAddress();

		JsonObject body = new JsonObject();
		body.addProperty("name", "Team Awesom");
		body.addProperty("description", service + " Service von Team Awesom");
		body.addProperty("service", service);
		body.addProperty("uri", "http://"+ipaddress+":4567/"+service);
		String bodyString = body.toString();
	
		HttpResponse<String> response = Unirest.post("http://172.18.0.5:4567/services")
				  .header("accept", "application/json")
				  .header("content-type", "application/json")
				  .body(bodyString)
				  .asString();
		System.out.println(response.getStatus());
//		System.out.println(response.getBody());
	}
	
	public static String getServices(String service) throws UnirestException {
		HttpRequest request = Unirest.get("http://172.18.0.5:4567/services/of/name/Team Awesom");

		HttpResponse<JsonNode> jsonResponse = request.asJson();
		Gson gson = new Gson();
		String responseJSONString = jsonResponse.getBody().toString();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(responseJSONString, listType);

		ArrayList<Object> services = (ArrayList<Object>)data.get("services");
		
		for (int i = 0; i < services.size(); i++) {
			HashMap<Object, Object> serviceMap = getService(services.get(i).toString());
			
			if(service.equals(serviceMap.get("service"))) {
				return serviceMap.get("uri").toString();
			}
		}
		return "false";
	}
	
	public static HashMap<Object, Object> getService(String serviceId) throws UnirestException {
		HttpRequest request = Unirest.get("http://172.18.0.5:4567"+serviceId);

		HttpResponse<JsonNode> jsonResponse = request.asJson();
		Gson gson = new Gson();
		String responseJSONString = jsonResponse.getBody().toString();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(responseJSONString, listType);
		
		return data;
	}
}
