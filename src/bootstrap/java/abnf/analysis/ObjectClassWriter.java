package abnf.analysis;

import abnf.node.ARule;


public final class ObjectClassWriter extends DepthFirstAdapter {



    @Override
    public void caseARule(final ARule node) {
        System.out.println("Rule->" + node.getRulename());
        super.caseARule(node);
    }



}
