package com.app.gerador_de_orcamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GeradorDeOrcamentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeradorDeOrcamentoApplication.class, args);
	}

}
