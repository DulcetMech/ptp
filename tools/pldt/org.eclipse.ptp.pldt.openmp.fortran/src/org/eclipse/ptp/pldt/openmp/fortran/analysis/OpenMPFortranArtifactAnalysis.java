/*******************************************************************************
 * Copyright (c) 2012 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.fortran.analysis;

import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.photran.internal.cdtinterface.core.FortranLanguage;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.ptp.pldt.common.IArtifactAnalysis;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.openmp.core.OpenMPScanReturn;
import org.eclipse.ptp.pldt.openmp.fortran.OpenMPFortranPlugin;

/**
 * OpenMP artifact analysis for Fortran.
 * <p>
 * Contributed to the <code>org.eclipse.ptp.pldt.mpi.core.artifactAnalysis</code> extension point.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class OpenMPFortranArtifactAnalysis implements IArtifactAnalysis {
	public ScanReturn runArtifactAnalysis(String languageID, ITranslationUnit tu, List<String> includes, boolean allowPrefixOnlyMatch) {
		final OpenMPScanReturn msr = new OpenMPScanReturn();
		if (languageID.equals(FortranLanguage.LANGUAGE_ID)) {
			IResource res = tu.getUnderlyingResource();
			if (!(res instanceof IFile))
				throw new IllegalStateException();
			try {
				ASTExecutableProgramNode ast = new Parser().parse(new ASTLexerFactory().createLexer((IFile) res));
				ast.accept(new OpenMPFortranASTVisitor(tu.getElementName(), msr));
			} catch (Exception e) {
				OpenMPFortranPlugin.log(e);
			}
		}
		return msr;
	}
}
