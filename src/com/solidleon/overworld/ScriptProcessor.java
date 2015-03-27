package com.solidleon.overworld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptProcessor {

	private static ScriptProcessor instance = new ScriptProcessor();
	
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine =  manager.getEngineByName("JavaScript");

	private ScriptProcessor() {
	}
	
	public static ScriptProcessor getInstance() {
		return instance;
	}
	
	public void executeProcess(ClientThread clientThread, String path, String line) throws ScriptException, FileNotFoundException, IOException, NoSuchMethodException {
		executeFunction(clientThread, path, "process", line);
	}

	public void executeFunction(ClientThread clientThread, String path, String functionName, Object... args) throws FileNotFoundException, IOException, ScriptException, NoSuchMethodException {
		File scriptFile = new File(path);
		Logging.info("Execute script function '%s' from '%s'", scriptFile.getAbsolutePath(), functionName);
		for (Object arg : args)
			Logging.info("ARG: '%s'", arg.toString());
			
		try (FileReader reader = new FileReader(scriptFile)) {
			engine.put("client", clientThread);
			engine.eval(reader);
			Invocable inv = (Invocable) engine;
			inv.invokeFunction(functionName, args);
		}
	}
	
}
