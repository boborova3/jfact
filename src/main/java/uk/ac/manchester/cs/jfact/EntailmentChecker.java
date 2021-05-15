package uk.ac.manchester.cs.jfact;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.io.Serializable;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;

import uk.ac.manchester.cs.jfact.kernel.ReasoningKernel;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleExpression;

/** entailment checker */
public class EntailmentChecker implements OWLAxiomVisitorEx<Boolean>, Serializable {

    private final ReasoningKernel kernel;
    private final TranslationMachinery tr;
    private final OWLDataFactory df;

    /**
     * @param k k
     * @param df df
     * @param tr tr
     */
    public EntailmentChecker(ReasoningKernel k, OWLDataFactory df, TranslationMachinery tr) {
        kernel = k;
        this.tr = tr;
        this.df = df;
    }

    @Override
    public Boolean visit(OWLSubClassOfAxiom axiom) {
        if (axiom.getSuperClass().equals(df.getOWLThing())
            || axiom.getSubClass().equals(df.getOWLNothing())) {
            return Boolean.TRUE;
        }
        ConceptExpression sub = tr.pointer(axiom.getSubClass());
        if (!kernel.isSatisfiable(sub)) {
            return Boolean.TRUE;
        }
        ConceptExpression sup = tr.pointer(axiom.getSuperClass());
        return Boolean.valueOf(kernel.isSubsumedBy(sub, sup));
    }

    @Override
    public Boolean visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isAsymmetric(tr.pointer(axiom.getProperty())));
    }

    @Override
    public Boolean visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isReflexive(tr.pointer(axiom.getProperty())));
    }

    @Override
    public Boolean visit(OWLDisjointClassesAxiom axiom) {
        return Boolean
            .valueOf(axiom.allMatch((a, b) -> kernel.isDisjoint(tr.pointer(a), tr.pointer(b))));
    }

    @Override
    public Boolean visit(OWLDataPropertyDomainAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLObjectPropertyDomainAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        return Boolean
            .valueOf(axiom.asSubObjectPropertyOfAxioms().stream().allMatch(this::acceptThis));
    }

    @Override
    public Boolean visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLDifferentIndividualsAxiom axiom) {
        return Boolean.valueOf(axiom.asOWLSubClassOfAxioms().stream().allMatch(this::acceptThis));
    }

    // TODO: this check is incomplete
    @Override
    public Boolean visit(OWLDisjointDataPropertiesAxiom axiom) {
        return Boolean.valueOf(
            axiom.allMatch((i, j) -> kernel.isDisjointRoles(tr.pointer(i), tr.pointer(j))));
    }

    @Override
    public Boolean visit(OWLDisjointObjectPropertiesAxiom axiom) {
        return Boolean.valueOf(
            axiom.allMatch((i, j) -> kernel.isDisjointRoles(tr.pointer(i), tr.pointer(j))));
    }

    @Override
    public Boolean visit(OWLObjectPropertyRangeAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLObjectPropertyAssertionAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isFunctional(tr.pointer(axiom.getProperty())));
    }

    @Override
    public Boolean visit(OWLSubObjectPropertyOfAxiom axiom) {
        return Boolean.valueOf(kernel.isSubRoles(tr.pointer(axiom.getSubProperty()),
            tr.pointer(axiom.getSuperProperty())));
    }

    @Override
    public Boolean visit(OWLDisjointUnionAxiom axiom) {
        return Boolean.valueOf(axiom.getOWLEquivalentClassesAxiom().accept(this).booleanValue()
            && axiom.getOWLDisjointClassesAxiom().accept(this).booleanValue());
    }

    @Override
    public Boolean visit(OWLDeclarationAxiom axiom) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(OWLAnnotationAssertionAxiom axiom) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isSymmetric(tr.pointer(axiom.getProperty())));
    }

    @Override
    public Boolean visit(OWLDataPropertyRangeAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLFunctionalDataPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isFunctional(tr.pointer(axiom.getProperty())));
    }

    @Override
    public Boolean visit(OWLEquivalentDataPropertiesAxiom axiom) {
        return Boolean
            .valueOf(axiom.asSubDataPropertyOfAxioms().stream().allMatch(this::acceptThis));
    }

    @Override
    public Boolean visit(OWLClassAssertionAxiom axiom) {
        return Boolean.valueOf(kernel.isInstance(tr.pointer(axiom.getIndividual()),
            tr.pointer(axiom.getClassExpression())));
    }

    @Override
    public Boolean visit(OWLEquivalentClassesAxiom axiom) {
        return Boolean
            .valueOf(axiom.allMatch((a, b) -> kernel.isEquivalent(tr.pointer(a), tr.pointer(b))));
    }

    @Override
    public Boolean visit(OWLDataPropertyAssertionAxiom axiom) {
        return axiom.asOWLSubClassOfAxiom().accept(this);
    }

    @Override
    public Boolean visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isTransitive(tr.pointer(axiom.getProperty())));
    }

    @Override
    public Boolean visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isIrreflexive(tr.pointer(axiom.getProperty())));
    }

    // TODO: this is incomplete
    @Override
    public Boolean visit(OWLSubDataPropertyOfAxiom axiom) {
        return Boolean.valueOf(kernel.isSubRoles(tr.pointer(axiom.getSubProperty()),
            tr.pointer(axiom.getSuperProperty())));
    }

    @Override
    public Boolean visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return Boolean.valueOf(kernel.isInverseFunctional(tr.pointer(axiom.getProperty())));
    }

    @Override
    public Boolean visit(OWLSameIndividualAxiom axiom) {
        return Boolean.valueOf(axiom.allMatch(
            (indA, indB) -> kernel.isSameIndividuals(tr.pointer(indA), tr.pointer(indB))));
    }

    @Override
    public Boolean visit(OWLSubPropertyChainOfAxiom axiom) {
        List<ObjectRoleExpression> l = asList(axiom.getPropertyChain().stream().map(tr::pointer));
        return Boolean.valueOf(kernel.isSubChain(tr.pointer(axiom.getSuperProperty()), l));
    }

    @Override
    public Boolean visit(OWLInverseObjectPropertiesAxiom axiom) {
        return Boolean
            .valueOf(axiom.asSubObjectPropertyOfAxioms().stream().allMatch(this::acceptThis));
    }

    @Override
    public Boolean visit(OWLHasKeyAxiom axiom) {
        // FIXME!! unsupported by FaCT++ ATM
        throw new UnsupportedEntailmentTypeException(axiom);
    }

    @Override
    public Boolean visit(OWLDatatypeDefinitionAxiom axiom) {
        // FIXME!! unsupported by FaCT++ ATM
        throw new UnsupportedEntailmentTypeException(axiom);
    }

    @Override
    public Boolean visit(SWRLRule rule) {
        // FIXME!! unsupported by FaCT++ ATM
        throw new UnsupportedEntailmentTypeException(rule);
    }

    @Override
    public Boolean visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(OWLAnnotationPropertyDomainAxiom axiom) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(OWLAnnotationPropertyRangeAxiom axiom) {
        return Boolean.FALSE;
    }

    protected boolean acceptThis(OWLAxiom ax) {
        return ax.accept(this).booleanValue();
    }
}
