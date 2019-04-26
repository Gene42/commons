/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import com.gene42.commons.utils.web.HttpEndpoint;

import org.phenotips.data.api.EntitySearchResult;
import org.phenotips.data.api.internal.builder.FamilySearchBuilder;
import org.phenotips.data.api.internal.builder.PatientSearchBuilder;
import org.phenotips.data.api.internal.filter.StringFilter;
import org.phenotips.data.rest.LiveTableInputAdapter;
import org.phenotips.data.rest.internal.adapter.URLInputAdapter;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class FamilyTableInputAdapterTest
{

    @Test
    public void test0() throws Exception {

        //String urlstr= "outputSyntax=plain&transprefix=patient.livetable.&classname=PhenoTips.PatientClass&collist=doc.name%2Cexternal_id%2Cdoc.creator%2Cdoc.author%2Cdoc.creationDate%2Cdoc.date%2Cfirst_name%2Clast_name%2Creference&queryFilters=currentlanguage%2Chidden&&filterFrom=%2C+LongProperty+iid&filterWhere=and+iid.id.id+%3D+obj.id+and+iid.id.name+%3D+%27identifier%27+and+iid.value+>%3D+0&offset=1&limit=25&reqNo=1&visibility=private&visibility=public&visibility=open&visibility%2Fclass=PhenoTips.VisibilityClass&owner%2Fclass=PhenoTips.OwnerClass&omim_id%2Fjoin_mode=OR&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&status=candidate&status=solved&reference%2Fclass=PhenoTips.FamilyReferenceClass&sort=doc.name&dir=asc";
        String urlStr = "outputSyntax=plain&transprefix=patient.livetable.&classname=PhenoTips.PatientClass&collist=doc.name%2Cexternal_id%2Cdoc.creator%2Cdoc.author%2Cdoc.creationDate%2Cdoc.date%2Cfirst_name%2Clast_name%2Creference&queryFilters=currentlanguage%2Chidden&&filterFrom=%2C+LongProperty+iid&filterWhere=and+iid.id.id+%3D+obj.id+and+iid.id.name+%3D+%27identifier%27+and+iid.value+%3E%3D+0&offset=1&limit=25&reqNo=1&visibility=private&visibility=public&visibility=open&visibility%2Fclass=PhenoTips.VisibilityClass&owner%2Fclass=PhenoTips.OwnerClass&omim_id%2Fjoin_mode=OR&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&status=candidate&status=solved&reference%2Fclass=PhenoTips.FamilyReferenceClass&sort=doc.name&dir=asc";
        String urlStr2 = "outputSyntax=plain&transprefix=family.livetable.&classname=PhenoTips.FamilyClass&collist=doc.name%2Cexternal_id%2Cproband_id%2Cindividuals%2Cdescription%2Canalysis_status%2Cdoc.creator%2Cdoc.creationDate%2Cdoc.author%2Cdoc.date&queryFilters=currentlanguage%2Chidden&&offset=1&limit=25&reqNo=1&external_id%2Fdoc_class=0%2FPhenoTips.FamilyClass%2C1%2FPhenoTips.PatientClass&doc.creator%2Fdoc_class=PhenoTips.PatientClass&owner%2Fclass=PhenoTips.OwnerClass&owner%2Fdoc_class=PhenoTips.PatientClass&doc.author%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fjoin_mode=OR&phenotype%2Fdoc_class=PhenoTips.PatientClass&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&phenotype_subterms%2Fdoc_class=PhenoTips.PatientClass&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&gene%2Fdoc_class=PhenoTips.PatientClass&status%2Fdoc_class=PhenoTips.PatientClass&status=candidate&status=solved&sort=doc.name&dir=asc";
        String urlStr3 = "outputSyntax=plain&transprefix=family.livetable.&classname=PhenoTips.FamilyClass&collist=doc.name%2Cexternal_id%2Cproband_id%2Cindividuals%2Cdescription%2Canalysis_status%2Cdoc.creator%2Cdoc.creationDate%2Cdoc.author%2Cdoc.date&queryFilters=currentlanguage%2Chidden&&offset=1&limit=25&reqNo=1&external_id%2Fdoc_class=0%2FPhenoTips.FamilyClass%2C1%2FPhenoTips.PatientClass&doc.creator%2Fdoc_class=PhenoTips.PatientClass&owner%2Fclass=PhenoTips.OwnerClass&owner%2Fdoc_class=PhenoTips.PatientClass&doc.author%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fjoin_mode=OR&phenotype%2Fdoc_class=PhenoTips.PatientClass&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&phenotype_subterms%2Fdoc_class=PhenoTips.PatientClass&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&gene%2Fdoc_class=PhenoTips.PatientClass&status%2Fdoc_class=PhenoTips.PatientClass&status=candidate&status=solved&sort=doc.name&dir=asc";
        String urlStr4 = "outputSyntax=plain&transprefix=family.livetable.&classname=PhenoTips.FamilyClass&collist=doc.name%2Cexternal_id%2Cproband_id%2Cindividuals%2Cdescription%2Canalysis_status%2Cdoc.creator%2Cdoc.creationDate%2Cdoc.author%2Cdoc.date&queryFilters=currentlanguage%2Chidden&&offset=1&limit=25&reqNo=4&external_id%2Fdoc_class=0%2FPhenoTips.FamilyClass%2C1%2FPhenoTips.PatientClass&external_id%2F1%40=P001&doc.creator%2Fdoc_class=PhenoTips.PatientClass&owner%2Fclass=PhenoTips.OwnerClass&owner%2Fdoc_class=PhenoTips.PatientClass&doc.author%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fjoin_mode=OR&phenotype%2Fdoc_class=PhenoTips.PatientClass&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&phenotype_subterms%2Fdoc_class=PhenoTips.PatientClass&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&gene%2Fdoc_class=PhenoTips.PatientClass&status%2Fdoc_class=PhenoTips.PatientClass&status=candidate&status=solved&sort=doc.name&dir=asc";
        String urlStr5 = "outputSyntax=plain&transprefix=family.livetable.&classname=PhenoTips.FamilyClass&collist=doc.name%2Cexternal_id%2Cproband_id%2Cindividuals%2Cdescription%2Canalysis_status%2Cdoc.creator%2Cdoc.creationDate%2Cdoc.author%2Cdoc.date&queryFilters=currentlanguage%2Chidden&&offset=1&limit=25&reqNo=1&external_id%2Fdoc_class=0%2FPhenoTips.FamilyClass%2C1%2FPhenoTips.PatientClass&external_id%2F1%40=003&external_id%2Fdoc_class=0%2FPhenoTips.FamilyClass%2C1%2FPhenoTips.PatientClass&doc.creator%2Fdoc_class=PhenoTips.PatientClass&owner%2Fclass=PhenoTips.OwnerClass&owner%2Fdoc_class=PhenoTips.PatientClass&doc.author%2Fdoc_class=PhenoTips.PatientClass&date_of_birth%2Fdoc_class=1%2FPhenoTips.PatientClass&doc.fullName%2Fref_values=-1%7CPhenoTips.FamilyClass%7Cproband_id&doc.fullName%2Fdoc_class=PhenoTips.PatientClass&doc.fullName%2Fmatch=exact&doc.fullName%2FdependsOn=date_of_birth&omim_id%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fjoin_mode=OR&phenotype%2Fdoc_class=PhenoTips.PatientClass&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&phenotype_subterms%2Fdoc_class=PhenoTips.PatientClass&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&gene%2Fdoc_class=PhenoTips.PatientClass&status%2Fdoc_class=PhenoTips.PatientClass&status=candidate&status=solved&sort=doc.name&dir=asc";
        String urlStr6 = "outputSyntax=plain&transprefix=family.livetable.&classname=PhenoTips.FamilyClass&collist=doc.name%2Cexternal_id%2Cproband_id%2Cindividuals%2Cdescription%2Canalysis_status%2Cdoc.creator%2Cdoc.creationDate%2Cdoc.author%2Cdoc.date&queryFilters=currentlanguage%2Chidden&&offset=1&limit=25&reqNo=1&external_id%2Fdoc_class=0%2FPhenoTips.FamilyClass&external_id%2Fdoc_class=1%2FPhenoTips.PatientClass&doc.creator%2Fdoc_class=PhenoTips.PatientClass&owner%2Fclass=PhenoTips.OwnerClass&owner%2Fdoc_class=PhenoTips.PatientClass&doc.author%2Fdoc_class=PhenoTips.PatientClass&date_of_birth%2Fdoc_class=1%2FPhenoTips.PatientClass&identifier%2F1%40ref_values=-1%7CPhenoTips.FamilyClass%7Cproband_id&identifier%2Fdoc_class=1%2FPhenoTips.PatientClass&omim_id%2Fdoc_class=PhenoTips.PatientClass&omim_id%2Fjoin_mode=OR&phenotype%2Fdoc_class=PhenoTips.PatientClass&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&phenotype_subterms%2Fdoc_class=PhenoTips.PatientClass&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&gene%2Fdoc_class=PhenoTips.PatientClass&status%2Fdoc_class=PhenoTips.PatientClass&status=candidate&status=solved&sort=doc.name&dir=asc";
        //FamilyTableInputAdapter

        String urlString = urlStr5;


        String urlnew = "outputSyntax=plain&transprefix=family.livetable.&classname=PhenoTips.FamilyClass&collist=doc.name%2Cexternal_id%2Cproband_id%2Cindividuals%2Cdescription%2Canalysis_status%2Cdoc.creator%2Cdoc.creationDate%2Cdoc.author%2Cdoc.date&queryFilters=currentlanguage%2Chidden&&offset=1&limit=25&reqNo=1&analysis_status%2Fclass=PhenoTips.FamilyMetaClass&analysis_status%2Fjoin_mode=or&owner%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass~(or%230)=PhenoTips.OwnerClass&collaborator%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass~(or%230)=PhenoTips.CollaboratorClass&visibility%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass~(or%230)=PhenoTips.VisibilityClass&visibility%2F%40PhenoTips.FamilyClass~PhenoTips.PatientClass~(or%230)=%24public&visibility%2F%40PhenoTips.FamilyClass~PhenoTips.PatientClass~(or%230)=%24open&reference%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.FamilyReferenceClass&reference%2Ftype%40PhenoTips.FamilyClass~PhenoTips.PatientClass=reference&owner%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.OwnerClass&omim_id%2Fjoin_mode%40PhenoTips.FamilyClass~PhenoTips.PatientClass=OR&phenotype%2Fjoin_mode%40PhenoTips.FamilyClass~PhenoTips.PatientClass=OR&phenotype%2Fsubterms%40PhenoTips.FamilyClass~PhenoTips.PatientClass=yes&gene%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.GeneClass&gene%2Fmatch%40PhenoTips.FamilyClass~PhenoTips.PatientClass=ci&status%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.GeneClass&status%2Fjoin_mode%40PhenoTips.FamilyClass~PhenoTips.PatientClass=OR&status%2FdependsOn%40PhenoTips.FamilyClass~PhenoTips.PatientClass=gene&status%2F%40PhenoTips.FamilyClass~PhenoTips.PatientClass=candidate&status%2F%40PhenoTips.FamilyClass~PhenoTips.PatientClass=solved&sort=doc.name&dir=asc";
        String urlnew2 = "outputSyntax=plain&transprefix=family.livetable.&classname=PhenoTips.FamilyClass&collist=doc.name%2Cexternal_id%2Cproband_id%2Cindividuals%2Csummary%2Canalysis_status%2Cdoc.creator%2Cdoc.creationDate%2Cdoc.author%2Cdoc.date&queryFilters=currentlanguage%2Chidden&summary_class=PhenoTips.FamilyMetaClass&&offset=1&limit=25&reqNo=3&analysis_status%2Fclass=PhenoTips.FamilyMetaClass&analysis_status%2Fjoin_mode=or&doc.fullName%2Fref_values%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=-1%7CPhenoTips.FamilyClass%7Cproband_id&doc.fullName%2FdependsOn%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=lower_year_of_birth%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)&lower_year_of_birth%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=PhenoTips.EncryptedPatientDataClass&reference%2FdependsOn%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=lower_year_of_birth%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)&reference%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=PhenoTips.FamilyReferenceClass&reference%2Ftype%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=reference&identifier%2Fmin%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=0&identifier%2Fvalidates_query%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=false&identifier%2FdependsOn%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)=lower_year_of_birth%40PhenoTips.FamilyClass~PhenoTips.PatientClass(1)&reference%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.FamilyReferenceClass&reference%2Ftype%40PhenoTips.FamilyClass~PhenoTips.PatientClass=reference&owner%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.OwnerClass&owner=XWiki.JaneJones&omim_id%2Fjoin_mode%40PhenoTips.FamilyClass~PhenoTips.PatientClass=OR&phenotype%2Fjoin_mode%40PhenoTips.FamilyClass~PhenoTips.PatientClass=OR&phenotype%2Fsubterms%40PhenoTips.FamilyClass~PhenoTips.PatientClass=yes&gene%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.GeneClass&gene%2Fmatch%40PhenoTips.FamilyClass~PhenoTips.PatientClass=ci&status%2Fclass%40PhenoTips.FamilyClass~PhenoTips.PatientClass=PhenoTips.GeneClass&status%2Fjoin_mode%40PhenoTips.FamilyClass~PhenoTips.PatientClass=OR&status%2FdependsOn%40PhenoTips.FamilyClass~PhenoTips.PatientClass=gene&status%2F%40PhenoTips.FamilyClass~PhenoTips.PatientClass=candidate&status%2F%40PhenoTips.FamilyClass~PhenoTips.PatientClass=solved&identifier%2Fmin%40PhenoTips.FamilyClass~PhenoTips.PatientClass=0&identifier%2Fvalidates_query%40PhenoTips.FamilyClass~PhenoTips.PatientClass=false&sort=doc.name&dir=asc";
        String testURLVisib = "outputSyntax=plain&transprefix=patient.livetable.&classname=PhenoTips.PatientClass&collist=reference%2Cdoc.name%2Cfirst_name%2Clast_name%2Ccollaborator%2Ccollaborator&queryFilters=currentlanguage%2Chidden&&offset=1&limit=25&reqNo=9&owner%2Fclass%40PhenoTips.PatientClass~(or%230)=PhenoTips.OwnerClass&owner%2Fmatch%40PhenoTips.PatientClass~(or%230)=exact&owner%2F%40PhenoTips.PatientClass~(or%230)=xwiki%3AXWiki.gigi&collaborator%2Fclass%40PhenoTips.PatientClass~(or%230)~(and%230)=PhenoTips.CollaboratorClass&collaborator%2Fmatch%40PhenoTips.PatientClass~(or%230)~(and%230)=exact&collaborator%2F%40PhenoTips.PatientClass~(or%230)~(and%230)=xwiki%3AXWiki.gigi&access%2Fclass%40PhenoTips.PatientClass~(or%230)~(and%230)=PhenoTips.CollaboratorClass&access%2Fmatch%40PhenoTips.PatientClass~(or%230)~(and%230)=exact&access%2F%40PhenoTips.PatientClass~(or%230)~(and%230)=manage&reference%2Fclass=PhenoTips.FamilyReferenceClass&collaborator%2Fclass=PhenoTips.CollaboratorClass&sort=collaborator&dir=asc";
        String testEncrypt = "outputSyntax=plain&transprefix=patient.livetable.&classname=PhenoTips.PatientClass&collist=doc.name%2Cexternal_id%2Cdoc.creator%2Cdoc.author%2Cdoc.creationDate%2Cdoc.date%2Cfirst_name%2Clast_name%2Creference&queryFilters=currentlanguage%2Chidden&first_name_class=PhenoTips.EncryptedPatientDataClass&last_name_class=PhenoTips.EncryptedPatientDataClass&&filterFrom=%2C+LongProperty+iid&filterWhere=and+iid.id.id+%3D+obj.id+and+iid.id.name+%3D+%27identifier%27+and+iid.value+%3E%3D+0&offset=1&limit=25&reqNo=1&visibility=private&visibility=public&visibility=open&visibility%2Fclass=PhenoTips.VisibilityClass&owner%2Fclass=PhenoTips.OwnerClass&initial%2Fclass=PhenoTips.EncryptedPatientDataClass&lower_year_of_birth%2Fclass=PhenoTips.EncryptedPatientDataClass&upper_year_of_birth%2Fclass=PhenoTips.EncryptedPatientDataClass&lower_year_of_death%2Fclass=PhenoTips.EncryptedPatientDataClass&upper_year_of_death%2Fclass=PhenoTips.EncryptedPatientDataClass&omim_id%2Fjoin_mode=OR&phenotype%2Fjoin_mode=OR&phenotype%2Fsubterms=yes&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&status=candidate&status=solved&identifier%2Fmin%40PhenoTips.PatientClass=0&reference%2Fclass=PhenoTips.FamilyReferenceClass&sort=doc.name&dir=asc";
        String yetAnother = "outputSyntax=plain&transprefix=patient.livetable.&classname=PhenoTips" +
            ".PatientClass&collist=doc.name,external_id,owner,doc.author,doc.creationDate,doc.date,first_name,last_name,reference&queryFilters=currentlanguage,hidden&&filterFrom=&filterWhere=&offset=1&limit=50&reqNo=1&user-hash=-1415488523&visibility=private&visibility=public&visibility=open&visibility/class=PhenoTips.VisibilityClass&visibility/match=exact&owner/class=PhenoTips.OwnerClass&owner/match=fullReference&collaborator/class=PhenoTips.CollaboratorClass&collaborator/match=fullReference&collaborator/join_mode=OR&clinical_diagnosis/join_mode=OR&omim_id/join_mode=OR&phenotype/join_mode=OR&phenotype_subterms=yes&gene/class=PhenoTips.GeneClass&gene/match=ci&status/class=PhenoTips.GeneClass&status/join_mode=OR&status/dependsOn=gene&status=candidate&status=solved&owner/class=PhenoTips.OwnerClass&reference/class=PhenoTips.FamilyReferenceClass&sort=doc.name&dir=asc";
        Map<String, List<String>> queryParameters = RequestUtils.getQueryParameters(testEncrypt);
        //Map<String, List<String>> queryParameters = RequestUtils.getQueryParameters(urlString);

        //UriInfo uriInfo = Mockito.mock(UriInfo.class);
       // Mockito.when(uriInfo.getQueryParameters()).thenReturn(queryParameters);




        String testURl = "outputSyntax=plain&transprefix=patient.livetable.&classname=PhenoTips.PatientClass&collist=doc.name,external_id,doc.creator,doc.author,doc.creationDate,doc.date,first_name,last_name,reference&queryFilters=currentlanguage,hidden&&filterFrom=, LongProperty iid&filterWhere=and iid.id.id = obj.id and iid.id.name = 'identifier' and iid.value >= 0&offset=1&limit=25&reqNo=1&visibility=private&visibility=public&visibility=open&visibility/class=PhenoTips.VisibilityClass&owner/class=PhenoTips.OwnerClass&initial/class=PhenoTips.EncryptedPatientDataClass&lower_year_of_birth/class=PhenoTips.EncryptedPatientDataClass&upper_year_of_birth/class=PhenoTips.EncryptedPatientDataClass&lower_year_of_death/class=PhenoTips.EncryptedPatientDataClass&upper_year_of_death/class=PhenoTips.EncryptedPatientDataClass&date_of_birth/class=PhenoTips.EncryptedPatientDataClass&omim_id/join_mode=OR&phenotype/join_mode=OR&phenotype_subterms=yes&gene/class=PhenoTips.GeneClass&gene/match=ci&status/class=PhenoTips.GeneClass&status/join_mode=OR&status/dependsOn=gene&status=candidate&status=solved&reference/class=PhenoTips.FamilyReferenceClass&sort=doc.name&dir=asc";
        String testURl2 = "outputSyntax=plain&transprefix=patient.livetable.&classname=PhenoTips.PatientClass&collist=doc.name%2Cexternal_id%2Cdoc.creator%2Cdoc.author%2Cdoc.creationDate%2Cdoc.date%2Cfirst_name%2Clast_name%2Creference&queryFilters=currentlanguage%2Chidden&&filterFrom=%2C+LongProperty+iid&filterWhere=and+iid.id.id+%3D+obj.id+and+iid.id.name+%3D+%27identifier%27+and+iid.value+%3E%3D+0&offset=1&limit=25&reqNo=1&visibility=private&visibility=public&visibility=open&visibility%2Fclass=PhenoTips.VisibilityClass&owner%2Fclass=PhenoTips.OwnerClass&omim_id%2Fjoin_mode=OR&phenotype%2Fjoin_mode=OR&phenotype_subterms=yes&gene%2Fclass=PhenoTips.GeneClass&gene%2Fmatch=ci&status%2Fclass=PhenoTips.GeneClass&status%2Fjoin_mode=OR&status%2FdependsOn=gene&status=candidate&status=solved&reference%2Fclass=PhenoTips.FamilyReferenceClass&sort=doc.name&dir=asc";


        for (Map.Entry<String, List<String>> entry : RequestUtils.getQueryParameters(yetAnother).entrySet()) {
            if (entry == null) {
                continue;
            }

            if (CollectionUtils.isEmpty(entry.getValue())) {
                System.out.println(String.format("[%1$s]=%2$s", entry.getKey(), entry.getValue()));
                continue;
            }

            for (int i = 0, len = entry.getValue().size(); i < len; i++) {
                String entryValue = (entry.getValue().get(i));
                //i//f (entryValue == null) {
                    System.out.println(String.format("[%1$s]=%2$s", entry.getKey(), entryValue));
               // }
               // else {
               //     System.out.println(String.format("[%1$s]=%2$s", entry.getKey(), param));
               // }

            }
        }

        /*Map<String, JSONObject> filters = FamilyTableInputAdapter.getFilters(queryParameters);

        for (Map.Entry<String, JSONObject> entry : filters.entrySet()) {
            System.out.println(String.format("%1$s=%2$s", entry.getKey(), entry.getValue().toString(4)));
        }*/

        //LiveTableInputAdapter adapter = new FamilyTableInputAdapter();
        LiveTableInputAdapter adapter  = new URLInputAdapter();
        JSONObject result = adapter.convert(queryParameters);

        System.out.println("RESULT=" + result.toString(4));
    }

    @Ignore
    @Test
    public void testEntitySearchRestEndpoint() throws Exception
    {
        try (EntitySearchRestEndpoint entitySearchRestEndpoint  = new EntitySearchRestEndpoint(HttpEndpoint
            .builder()
            .setHost("local.phenotips.com")
            .setUsernameAndPassword("Admin", "admin")
            .build())) {

            EntitySearchRequestBuilder builder = new EntitySearchRequestBuilder()
                .setDocumentSearchBuilder(new FamilySearchBuilder().newExternalIdFilter()
                                                                   .setMatch(StringFilter.MATCH_SUBSTRING)
                                                                   .setValue("mrn")
                                                                   .back())
                .addObjectTableColumn("proband_id", FamilySearchBuilder.FAMILY_CLASS)
                .addObjectTableColumn("members", FamilySearchBuilder.FAMILY_CLASS)
                .addObjectTableColumn("external_id", FamilySearchBuilder.FAMILY_CLASS);



            EntitySearchResult<JSONObject> searchResult = entitySearchRestEndpoint.search(builder);


            JSONObject result = new JSONObject(searchResult);

            System.out.println(result.toString(4));

            System.out.println(new JSONObject(
                entitySearchRestEndpoint.getHttpEndpoint()
                                        .performGetRequest("rest/patients/" + "P0000001")
            ).toString(4));


            System.out.println(new JSONObject(entitySearchRestEndpoint.search(
                new EntitySearchRequestBuilder()
                    .setDocumentSearchBuilder(new PatientSearchBuilder()
                        .newStringFilter("doc.name")
                        .setMatch(StringFilter.MATCH_EXACT)
                        .setValue("P0000001")
                        .back()
                        .setLimit(1))
                    .addObjectTableColumn("reference", "PhenoTips.FamilyReferenceClass"))
            ).toString(4));
        }
    }

}
