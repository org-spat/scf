package org.spat.scf.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;

public abstract class AbstractSCFMojo extends AbstractMojo {

	/**
	 * @parameter
	 */
	protected String scfhome;
	
	/**
	 * @parameter
	 */
	protected String name;
	
	/**
	 * @parameter
	 */
	protected List<Dependency> dependencies;
	
	 /**
     * @parameter expression = "${project.basedir}"
     * @required
     * @readonly
     */
	protected File basedir;
	


	/**
	 * @parameter expression ="${localRepository}"
	 * @required
	 * @readonly
	 */
	protected ArtifactRepository localRepository;

	public String getScfhome() {
		if(scfhome==null || scfhome.length()==0){
			scfhome = System.getenv("SCF_HOME");
		}
		return scfhome;
	}

	public void setScfhome(String scfhome) {
		this.scfhome = scfhome;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}
	public File getBasedir() {
		return basedir;
	}

	public ArtifactRepository getLocalRepository() {
		return localRepository;
	}
	
}
