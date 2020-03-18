package org.dbm.dbd.web.tag.dialect;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.dbm.dbd.web.tag.processor.AuthorizeAttrProcessor;

import java.util.HashSet;
import java.util.Set;

public class VoDialect extends AbstractProcessorDialect {

    public VoDialect() {
        super("Dbd Dialect", "dbd", StandardDialect.PROCESSOR_PRECEDENCE);
    }


    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new AuthorizeAttrProcessor(TemplateMode.HTML, dialectPrefix));//添加我们定义的标签
        return processors;
    }

}
