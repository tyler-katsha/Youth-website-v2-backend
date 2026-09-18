package com.tyler.YouthEngedi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
@EnableAsync
public class YouthEngediApplication {


	public static void main(String[] args) {
		loadDotenv();
		SpringApplication.run(YouthEngediApplication.class, args);
	}

	private static void loadDotenv(){
		var path = "../../../.env";

		File envFile = new File(path);
		
		if(!envFile.exists()){
			return;
		}

		try(BufferedReader reader = new BufferedReader(new FileReader(envFile))){
			String line;
			while((line = reader.readLine()) != null){
				line = line.trim();
				if(line.startsWith("#") || !line.contains("=")){
					continue;
				}

				var delimiterIndex = line.indexOf("=");
				var key = line.substring(0,delimiterIndex).trim();
				var value = line.substring(delimiterIndex + 1).trim();

				// Remove surrounding single or double quotes if present
				if((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))){
					value = value.substring(1,value.length() - 1);
				}

				System.out.println(key + " >>> " + value);
				System.setProperty(key,value);
			}
		} catch (IOException e){
			System.err.println("Failed to read .env file " + e.getMessage());
		}
	}

}
