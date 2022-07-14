package com.pwm.aws.crud.lambda.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.pwm.aws.crud.lambda.api.model.Product;

public class ProductLambdaHandler implements RequestStreamHandler {

	private String DYNAMO_TABLE = "Products";
	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		
		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser(); // this will help us parse the request object
		JSONObject responseObject = new JSONObject(); // we will add to this object for our api response
		JSONObject responseBody = new JSONObject();// we will add the item to this object
		
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(client);
		
		int id; 
		Item resItem = null;
		
		try {
			JSONObject reqObject = (JSONObject) parser.parse(reader);
			//pathParameters
			if (reqObject.get("pathParameters")!=null) {
				JSONObject pps = (JSONObject)reqObject.get("pathParameters");
				if (pps.get("id")!=null) {
					id = Integer.parseInt((String)pps.get("id"));
					resItem = dynamoDB.getTable(DYNAMO_TABLE).getItem("id",id);
					
				}
			}
			//queryStringParameters
			else if (reqObject.get("queryStringParameters")!=null) {
				JSONObject qps =(JSONObject) reqObject.get("queryStringParameters");
				if (qps.get("id")!=null) {
					id= Integer.parseInt((String)qps.get("id"));
					resItem = dynamoDB.getTable(DYNAMO_TABLE).getItem("id",id);
					
				}
			}
			
			if (resItem!=null) {
				Product product = new Product(resItem.toJSON());
				responseBody.put("product", product);
				responseObject.put("statusCode", 200);
			}else {
				responseBody.put("message", "No Items Found");
				responseObject.put("statusCode", 404);
			}
			
			responseObject.put("body", responseBody.toString());
			
		} catch (Exception e) {
			context.getLogger().log("ERROR : "+e.getMessage());
		}
		writer.write(responseObject.toString());
		reader.close();
		writer.close();
		
	}
	
	@SuppressWarnings("unchecked")
	public void handlePutRequest(InputStream input, OutputStream output, Context context) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser(); // this will help us parse the request object
		JSONObject responseObject = new JSONObject(); // we will add to this object for our api response
		JSONObject responseBody = new JSONObject();// we will add the item to this object
		
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(client);
		
		try {
			JSONObject reqObject =(JSONObject) parser.parse(reader);
			
			if (reqObject.get("body")!=null) {
				Product product = new Product((String)reqObject.get("body"));
				
				dynamoDB.getTable(DYNAMO_TABLE)
				.putItem(new PutItemSpec().withItem(new Item()
						.withNumber("id", product.getId())
						.withString("name", product.getName())
						.withNumber("price", product.getPrice())));
				responseBody.put("message", "New Item created/updated");
				responseObject.put("statusCode", 200);
				responseObject.put("body", responseBody.toString());
			}
			
			
		} catch (Exception e) {
			responseObject.put("statusCode", 400);
			responseObject.put("error", e);
		}
		
		writer.write(responseObject.toString());
		reader.close();
		writer.close();
	}
	
	
	@SuppressWarnings("unchecked")
	public void handleDeleteRequest(InputStream input, OutputStream output, Context context) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser(); // this will help us parse the request object
		JSONObject responseObject = new JSONObject(); // we will add to this object for our api response
		JSONObject responseBody = new JSONObject();// we will add the item to this object
		
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(client);
		
		try {
			JSONObject reqObject =(JSONObject) parser.parse(reader);
			
			if (reqObject.get("pathParameters")!=null) {
				JSONObject pps =(JSONObject) reqObject.get("pathParameters");
				
				if (pps.get("id")!=null) {
					int id = Integer.parseInt((String)pps.get("id"));
					dynamoDB.getTable(DYNAMO_TABLE).deleteItem("id",id);
				}
				
			}
			
			responseBody.put("message", "Item deleted");
			responseObject.put("statusCode", 200);
			responseObject.put("body", responseBody.toString());
			
			
		} catch (Exception e) {
			responseObject.put("statusCode", 400);
			responseObject.put("error", e);
		}
		
		writer.write(responseObject.toString());
		reader.close();
		writer.close();
	}

}





















