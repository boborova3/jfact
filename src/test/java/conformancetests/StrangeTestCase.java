package conformancetests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import testbase.TestBase;

class StrangeTestCase extends TestBase {

    @Test
    void shouldFindThreeSubclasses() throws OWLOntologyCreationException {
        OWLOntology o = m.createOntology();
        o.add(df.getOWLSubClassOfAxiom(df.getOWLClass(IRI.create("urn:b")),
            df.getOWLClass(IRI.create("urn:c"))));
        o.add(df.getOWLSubClassOfAxiom(df.getOWLClass(IRI.create("urn:a")),
            df.getOWLClass(IRI.create("urn:b"))));
        OWLReasoner r = factory().createReasoner(o);
        r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        NodeSet<OWLClass> subClasses = r.getSubClasses(df.getOWLClass(IRI.create("urn:c")), false);
        assertEquals(3, subClasses.entities().count());
    }
}
