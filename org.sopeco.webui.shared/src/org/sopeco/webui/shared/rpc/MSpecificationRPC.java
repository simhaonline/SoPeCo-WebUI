/**
 * Copyright (c) 2013 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.webui.shared.rpc;

import java.util.List;

import org.sopeco.persistence.entities.definition.MeasurementSpecification;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author Marius Oehler
 * @author Peter Merkert
 */
@RemoteServiceRelativePath("mSpecificationRPC")
public interface MSpecificationRPC extends RemoteService {

	/**
	 * Return a list with all specification names, of the selected scenario.
	 * 
	 * @return list with names
	 */
	List<String> getAllSpecificationNames();

	/**
	 * Return a list with all specifications.
	 * 
	 * @return list with names
	 */
	List<MeasurementSpecification> getAllSpecifications();

	/**
	 * Set the current working specification to the given specification.
	 * 
	 * @param specificationName
	 * @return
	 */
	boolean setWorkingSpecification(String specificationName);

	/**
	 * Creates a new specification with the given name.
	 * 
	 * @param name
	 * @return
	 */
	boolean createSpecification(String name);

	/**
	 * Renames the working specification to the new name.
	 * 
	 * @param newName
	 *            the new name
	 * @return
	 */
	boolean renameWorkingSpecification(String newName);
	
	/**
	 * Removes the current selected working specification of the given name.
	 * 
	 * @return true, if successful
	 */
	boolean removeWorkingSpecification();
}
