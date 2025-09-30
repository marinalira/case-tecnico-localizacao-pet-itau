package br.com.itaucase.localizacao_pet;

import org.springframework.boot.SpringApplication;

public class TestLocalizacaoPetApplication {

	public static void main(String[] args) {
		SpringApplication.from(LocalizacaoPetApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
