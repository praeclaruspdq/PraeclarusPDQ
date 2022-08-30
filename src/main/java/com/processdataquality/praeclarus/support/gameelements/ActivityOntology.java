/*
 * Copyright (c) 2022 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.support.gameelements;

import java.io.File;
import java.util.ArrayList;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.processdataquality.praeclarus.support.logelements.Activity;

public class ActivityOntology {

	private IRI IOR;
	private OWLOntologyManager man;
	private OWLOntology o;
	private OWLDataFactory df;
	private OWLDatatype xsdString;
	private OWLDatatype xsdDateTime;
	private OWLDatatype xsdDouble;
	private OWLClass activity;
	private OWLClass dataAttribute;
	private OWLObjectProperty hasData;
	private OWLObjectProperty hasPart;
	private OWLDataProperty caseID;
	private OWLDataProperty resource;
	private OWLDataProperty timeStamp;
	private OWLDataProperty suitability;
	private OWLDataProperty name;
	private OWLDataProperty value;

	public ActivityOntology(String iri) {
		try {
			IOR = IRI.create(iri);
			man = OWLManager.createOWLOntologyManager();			
			o = man.createOntology(IOR);
			df = o.getOWLOntologyManager().getOWLDataFactory();
			xsdString = df.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
			xsdDateTime = df.getOWLDatatype(OWL2Datatype.XSD_DATE_TIME.getIRI());
			xsdDouble = df.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createSchema() {
		activity = df.getOWLClass(IOR + "#activity");
		dataAttribute = df.getOWLClass(IOR + "#dataAttribute");
		o.add(df.getOWLDeclarationAxiom(activity));
		o.add(df.getOWLDeclarationAxiom(dataAttribute));

		hasData = df.getOWLObjectProperty(IOR + "#hasData");
		o.add(df.getOWLInverseFunctionalObjectPropertyAxiom(hasData));
		o.add(df.getOWLAsymmetricObjectPropertyAxiom(hasData));
		o.add(df.getOWLObjectPropertyDomainAxiom(hasData, activity));
		o.add(df.getOWLObjectPropertyRangeAxiom(hasData, dataAttribute));

		hasPart = df.getOWLObjectProperty(IOR + "#hasPart");
		o.add(df.getOWLTransitiveObjectPropertyAxiom(hasPart));

		caseID = df.getOWLDataProperty(IOR + "#caseID");
		o.add(df.getOWLFunctionalDataPropertyAxiom(caseID));
		o.add(df.getOWLDataPropertyDomainAxiom(caseID, activity));
		o.add(df.getOWLDataPropertyRangeAxiom(caseID, xsdString));

		resource = df.getOWLDataProperty(IOR + "#resource");
		o.add(df.getOWLFunctionalDataPropertyAxiom(resource));
		o.add(df.getOWLDataPropertyDomainAxiom(resource, activity));
		o.add(df.getOWLDataPropertyRangeAxiom(resource, xsdString));

		timeStamp = df.getOWLDataProperty(IOR + "#timestamp");
		o.add(df.getOWLFunctionalDataPropertyAxiom(timeStamp));
		o.add(df.getOWLDataPropertyDomainAxiom(timeStamp, activity));
		o.add(df.getOWLDataPropertyRangeAxiom(timeStamp, xsdDateTime));

		suitability = df.getOWLDataProperty(IOR + "#suitability");
		o.add(df.getOWLFunctionalDataPropertyAxiom(suitability));
		o.add(df.getOWLDataPropertyDomainAxiom(suitability, activity));
		o.add(df.getOWLDataPropertyRangeAxiom(suitability, xsdDouble));

		name = df.getOWLDataProperty(IOR + "#name");
		o.add(df.getOWLFunctionalDataPropertyAxiom(name));
		o.add(df.getOWLDataPropertyDomainAxiom(name, dataAttribute));
		o.add(df.getOWLDataPropertyRangeAxiom(name, xsdString));

		value = df.getOWLDataProperty(IOR + "#value");
		o.add(df.getOWLFunctionalDataPropertyAxiom(value));
		o.add(df.getOWLDataPropertyDomainAxiom(value, dataAttribute));
		o.add(df.getOWLDataPropertyRangeAxiom(value, xsdString));

	}

	public void addActivities(ArrayList<Activity> activities, ArrayList<RDFAnswer> approvedPairs) {
		for (Activity act : activities) {
			if (getActClass(act.getName()) == null) {
				OWLClass actClass = df.getOWLClass(IOR + "#" + act.getName());
				o.add(df.getOWLDeclarationAxiom(actClass));
				o.add(df.getOWLSubClassOfAxiom(actClass, activity));
			}
		}

		for (RDFAnswer apa : approvedPairs) {
			int relation = apa.getRelation();
			String extraLabel = apa.getExtraInfo();
			Activity act1 = apa.getLabel1();
			Activity act2 = apa.getLabel2();
			ArrayList<OWLClass> a12classes = new ArrayList<>();
			OWLClass act1Class = getActClass(act1.getName());
			OWLClass act2Class = getActClass(act2.getName());
			if (act1Class == null) {
				act1Class = df.getOWLClass(IOR + "#" + act1.getName());
				o.add(df.getOWLDeclarationAxiom(act1Class));
				o.add(df.getOWLSubClassOfAxiom(act1Class, activity));
			}
			if (act2Class == null) {
				act2Class = df.getOWLClass(IOR + "#" + act2.getName());
				o.add(df.getOWLDeclarationAxiom(act2Class));
				o.add(df.getOWLSubClassOfAxiom(act2Class, activity));
			}
			a12classes.add(act1Class);
			a12classes.add(act2Class);

			switch (relation) {

			case 1:
				if (extraLabel != null) {
					if (extraLabel.equals("NULL")) {
						o.add(df.getOWLEquivalentClassesAxiom(a12classes));
					} else {
						OWLClass extraClass = getActClass(extraLabel.replace(" ", "_"));
						if (extraClass == null) {
							OWLClass newExtraClass = df.getOWLClass(IOR + "#" + extraLabel.replace(" ", "_"));
							o.add(df.getOWLDeclarationAxiom(newExtraClass));
							o.add(df.getOWLSubClassOfAxiom(newExtraClass, activity));
							a12classes.add(newExtraClass);
						} else {
							a12classes.add(extraClass);
						}
						o.add(df.getOWLEquivalentClassesAxiom(a12classes));
					}
				}
				break;
			case 2:
				o.add(df.getOWLDisjointClassesAxiom(a12classes));
				break;
			case 3:
				o.add(df.getOWLSubClassOfAxiom(act2Class, act1Class));
				break;
			case 4:
				o.add(df.getOWLSubClassOfAxiom(act1Class, act2Class));
				break;
			case 5:
				if (extraLabel != null) {
					if (extraLabel.equals("NULL")) {
						OWLClass newSuperClass = df.getOWLClass(IOR + "#" + act1.getName() + "_OR_" + act2.getName());
						o.add(df.getOWLDeclarationAxiom(newSuperClass));
						o.add(df.getOWLSubClassOfAxiom(newSuperClass, activity));
						o.add(df.getOWLSubClassOfAxiom(act1Class, newSuperClass));
						o.add(df.getOWLSubClassOfAxiom(act2Class, newSuperClass));
					} else {
						OWLClass extraClass = getActClass(extraLabel.replace(" ", "_"));
						if (extraClass == null) {
							extraClass = df.getOWLClass(IOR + "#" + extraLabel.replace(" ", "_"));
							o.add(df.getOWLDeclarationAxiom(extraClass));
							o.add(df.getOWLSubClassOfAxiom(extraClass, activity));
						}
						o.add(df.getOWLSubClassOfAxiom(act1Class, extraClass));
						o.add(df.getOWLSubClassOfAxiom(act2Class, extraClass));
					}
				}
				break;
			case 6:
				o.add(df.getOWLSubClassOfAxiom(act1Class, df.getOWLObjectSomeValuesFrom(hasPart, act2Class)));
				break;
			case 7:
				o.add(df.getOWLSubClassOfAxiom(act2Class, df.getOWLObjectSomeValuesFrom(hasPart, act1Class)));
				break;
			case 8:
				if (extraLabel != null) {
					if (extraLabel.equals("NULL")) {
						OWLClass newSuperClass = df.getOWLClass(IOR + "#" + act1.getName() + "_AND_" + act2.getName());
						o.add(df.getOWLDeclarationAxiom(newSuperClass));
						o.add(df.getOWLSubClassOfAxiom(newSuperClass, activity));
						o.add(df.getOWLSubClassOfAxiom(newSuperClass,
								df.getOWLObjectSomeValuesFrom(hasPart, act1Class)));
						o.add(df.getOWLSubClassOfAxiom(newSuperClass,
								df.getOWLObjectSomeValuesFrom(hasPart, act2Class)));
					} else {
						OWLClass extraClass = getActClass(extraLabel.replace(" ", "_"));
						if (extraClass == null) {
							extraClass = df.getOWLClass(IOR + "#" + extraLabel.replace(" ", "_"));
							o.add(df.getOWLDeclarationAxiom(extraClass));
							o.add(df.getOWLSubClassOfAxiom(extraClass, activity));
						}
						o.add(df.getOWLSubClassOfAxiom(extraClass, df.getOWLObjectSomeValuesFrom(hasPart, act1Class)));
						o.add(df.getOWLSubClassOfAxiom(extraClass, df.getOWLObjectSomeValuesFrom(hasPart, act2Class)));
					}
				}
				break;
			}
		}
		addClousureAxioms();
	}

	private void addClousureAxioms() {
		ArrayList<OWLClass> classes = new ArrayList<OWLClass>();
		o.classesInSignature().forEach(classes::add);

		for (OWLClass c : classes) {
			ArrayList<OWLSubClassOfAxiom> subClsAxioms = new ArrayList<OWLSubClassOfAxiom>();
			o.subClassAxiomsForSubClass(c).forEach(subClsAxioms::add);
			ArrayList<OWLClassExpression> partClasses = new ArrayList<>();
			for (OWLSubClassOfAxiom sca : subClsAxioms) {
				OWLClassExpression ce = sca.getSuperClass();
				if (ce instanceof OWLObjectSomeValuesFrom) {
					OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) ce;
					OWLObjectPropertyExpression property = someValuesFrom.getProperty(); // always returns hasPart
					if (property.equals(hasPart)) {
						OWLClassExpression filler = someValuesFrom.getFiller(); // return the class in hasPart (the
																				// part)
						partClasses.add(filler);
					}
				}
			}
			if (partClasses.size() > 0) {
				OWLObjectUnionOf union = df.getOWLObjectUnionOf(partClasses);
				o.add(df.getOWLSubClassOfAxiom(c, df.getOWLObjectAllValuesFrom(hasPart, union)));
			}

		}
	}

	private OWLClass getActClass(String act) {
		ArrayList<OWLClass> classes = new ArrayList<OWLClass>();
		o.classesInSignature().forEach(classes::add);

		for (OWLClass c : classes) {
			if (c.getIRI().getRemainder().orElse("").equals(act)) {
				return c;
			}
		}
		return null;
	}

	public void startReasoner() throws Exception {
		OWLReasonerFactory rf = new ReasonerFactory();
		OWLReasoner r = rf.createReasoner(o);
		r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		ArrayList<OWLClass> classes = new ArrayList<OWLClass>();
		o.classesInSignature().forEach(classes::add);

	}
	
	public OWLOntology getOntology() {
		return o;
	}

}
