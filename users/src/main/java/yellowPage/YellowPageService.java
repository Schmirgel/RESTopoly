package yellowPage;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jetty.websocket.server.ServletWebSocketResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.*;

public class YellowPageService {

	public static void registerService(String service) throws Exception {
		InetAddress IP = InetAddress.getLocalHost();
		String ipaddress = IP.getHostAddress();
		
		YellowPage body = new YellowPage();
		body.setName("Team Awesom");
		body.setDescription(service + " Service von Team Awesom");
		body.setService(service);
		body.setUri("http://"+ipaddress+":4567/"+service);
		HttpResponse<JsonNode> response = Unirest
				.post("http://172.18.0.5:4567/services")
				.header("accept", "application/json")
				.header("Content-Type", "application/json")
				.body(body).asJson();
		System.out.println(response.getStatus());
		System.out.println(response.getBody());
	}
	
	public static String getServices(String service) throws UnirestException {
		HttpRequest request = Unirest.get("http://172.18.0.5:4567/services/of/name/Team Awesom");

		HttpResponse<JsonNode> jsonResponse = request.asJson();
		Gson gson = new Gson();
		String responseJSONString = jsonResponse.getBody().toString();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(responseJSONString, listType);

		ArrayList<Object> services = (ArrayList)data.get("services");
		
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
