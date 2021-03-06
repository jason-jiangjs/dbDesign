package org.dbm.dbd.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.dbm.dbd.web.tag.dialect.VoDialect;

@SpringBootApplication
@ComponentScan(basePackages = "org.dbm")
@ServletComponentScan(basePackages = {"org.dbm.dbd.web","com.mxgraph.online"})
public class DbDesignApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// 注意这里要指向原先用main方法执行的Application启动类
		return builder.sources(DbDesignApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(DbDesignApplication.class, args);
	}

	@Bean
	public VoDialect createVoDialect() {
		return new VoDialect();
	}

}
