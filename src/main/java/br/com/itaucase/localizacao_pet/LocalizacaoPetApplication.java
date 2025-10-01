package br.com.itaucase.localizacao_pet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LocalizacaoPetApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocalizacaoPetApplication.class, args);
	}

}
