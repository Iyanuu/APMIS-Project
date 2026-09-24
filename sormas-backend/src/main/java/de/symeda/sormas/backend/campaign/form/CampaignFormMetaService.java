package de.symeda.sormas.backend.campaign.form;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaGeographyLevel;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.campaign.Campaign;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.messaging.Message;
import de.symeda.sormas.backend.user.User;

@Stateless
@LocalBean
public class CampaignFormMetaService extends AdoServiceWithUserFilter<CampaignFormMeta> {

	@EJB
	private AreaService areaService;

	public CampaignFormMetaService() {
		super(CampaignFormMeta.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, CampaignFormMeta> from) {
		return null;
	}
	
	

	public Predicate buildCriteriaFilter(CampaignFormCriteria campaignFormCriteria, CriteriaBuilder cb,
			Root<CampaignFormMeta> from) {

		Predicate filter = null;
		if (campaignFormCriteria.getRelevanceStatus() != null) {
			if (campaignFormCriteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.or(cb.equal(from.get(CampaignFormMeta.ARCHIVED), false),
								cb.isNull(from.get(CampaignFormMeta.ARCHIVED))));
			} else if (campaignFormCriteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(CampaignFormMeta.ARCHIVED), true));
			}
		}

		if (campaignFormCriteria.getFormCategory() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(CampaignFormMeta.FORM_CATEGORY), campaignFormCriteria.getFormCategory()));
		}

		if (campaignFormCriteria.getModality() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(CampaignFormMeta.MODALITY), campaignFormCriteria.getModality()));
		}

		if (campaignFormCriteria.getFormType() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(CampaignFormMeta.FORM_TYPE), campaignFormCriteria.getFormType()));
		}

		if (campaignFormCriteria.getFormName() != null) {
			String[] textFilters = (campaignFormCriteria.getFormName().split("\\s+"));
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
						CriteriaBuilderHelper.unaccentedIlikeCustom(cb, from.get(CampaignFormMeta.FORM_NAME),
								textFilter),
						CriteriaBuilderHelper.ilike(cb, from.get(CampaignFormMeta.UUID), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		return filter;
	}

	public List<CampaignFormMeta> getAllAfter(Date since, User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMeta> cq = cb.createQuery(getElementClass());
		Root<CampaignFormMeta> root = cq.from(getElementClass());

		Predicate filter = createUserFilter(cb, cq, root);
		if (since != null) {
			Predicate dateFilter = createChangeDateFilter(cb, root, since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}
		if (filter != null) {
			cq.where(filter);
		}
		cq.orderBy(cb.desc(root.get(AbstractDomainObject.CHANGE_DATE)));

		List<CampaignFormMeta> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}
	

	

	public List<CampaignFormMeta> getAllFormElements(User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMeta> cq = cb.createQuery(getElementClass());
		Root<CampaignFormMeta> root = cq.from(getElementClass());

		Predicate filter = createUserFilter(cb, cq, root);
//		if (since != null) {
//			Predicate dateFilter = createChangeDateFilter(cb, root, since);
//			if (filter != null) {
//				filter = cb.and(filter, dateFilter);
//			} else {
//				filter = dateFilter;
//			}
//		}
		if (filter != null) {
			cq.where(filter);
		}
		cq.orderBy(cb.desc(root.get(AbstractDomainObject.CHANGE_DATE)));

		List<CampaignFormMeta> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaign(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID), campaignFormMetaJoin.get(CampaignFormMeta.FORMGROUPUUID), campaignFormMetaJoin.get(CampaignFormMeta.FORMVERSION),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignAndUserLanguage(String uuid,
			String userLanguage) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);

		if (userLanguage.equalsIgnoreCase("pashto")) {
			cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
					campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO));
		} else if (userLanguage.equalsIgnoreCase("dari")) {
			cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
					campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI));
		} else {
			cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
					campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));
		}

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignPashto(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignDari(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignIntraCampaign(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate typefilter = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), "intra-campaign");// cb.and

		cq = cq.where(filter, typefilter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignPostCampaign(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate typefilter = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), "post-campaign");// cb.and
//		Predicate filterxx = cb.equal(campaignRoot.get(Campaign.PUBLISHED), true);
		cq = cq.where(filter, typefilter);// filterxx);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

//		System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" + SQLExtractor.from(em.createQuery(cq)));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRound(String round,
			String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundx(String round,
			String uuid, AreaReferenceDto areaReferenceDto) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);
		Predicate filtery = null;

		if (areaReferenceDto != null) {
			filtery = buildCriteriaFilterArea(areaReferenceDto, cb, campaignFormMetaJoin);
		}

		Predicate filter = cb.and(filterc, filterx, filtery);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndPashto(String round,
			String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" + SQLExtractor.from(em.createQuery(cq)));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndPashtox(String round,
			String uuid, AreaReferenceDto areaReferenceDto) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);
		Predicate filtery = null;

		if (areaReferenceDto != null) {
			filtery = buildCriteriaFilterArea(areaReferenceDto, cb, campaignFormMetaJoin);
		}
		Predicate filter = cb.and(filterc, filterx, filtery);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" + SQLExtractor.from(em.createQuery(cq)));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndDari(String round,
			String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		return em.createQuery(cq).getResultList();
	}
	
	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndDarix(String round,
			String uuid, AreaReferenceDto areaReferenceDto) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);
		Predicate filtery = null;

		if (areaReferenceDto != null) {
			filtery = buildCriteriaFilterArea(areaReferenceDto, cb, campaignFormMetaJoin);
		}
		
		Predicate filter = cb.and(filterc, filterx, filtery);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		return em.createQuery(cq).getResultList();
	}
	
	public Predicate buildCriteriaFilterArea(AreaReferenceDto areaReferenceDto, CriteriaBuilder cb, Join from) {

		Predicate filter = null;
		if (areaReferenceDto != null) {
			Join<CampaignFormMeta, Area> joinAreas = from.join(CampaignFormMeta.AREA, JoinType.LEFT);
			Predicate areaFilter = cb.or(joinAreas.in(areaService.getByUuid(areaReferenceDto.getUuid())),
					cb.isNull(joinAreas));
			filter = CriteriaBuilderHelper.and(cb, filter, areaFilter);
		}
		return filter;
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundandForm(String round,
			String uuid, Set<FormAccess> userFormAccess) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

		return em.createQuery(cq).getResultList();
	}
	
	
//	public Boolean getDistrictEntryStatusByUuid(String formUUid) {
//	    CriteriaBuilder cb = em.getCriteriaBuilder();
//	    CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
//	    Root<CampaignFormMeta> root = cq.from(CampaignFormMeta.class);
//
//	    // Assuming there's a UUID column called "uuid" that you're matching against
//	    cq.select(root.get(CampaignFormMeta.GEOGRAPHYLEVEL))
//	      .where(cb.equal(root.get(CampaignFormMeta.UUID), formUUid));
//        return em.createQuery(cq).getSingleResult();
//
//	}
	
    public String getGeographyLevelStatusByUuid(String formUUid) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CampaignFormMetaGeographyLevel> cq = cb.createQuery(CampaignFormMetaGeographyLevel.class);
        Root<CampaignFormMeta> root = cq.from(CampaignFormMeta.class);

        cq.select(root.get(CampaignFormMeta.GEOGRAPHYLEVEL))
          .where(cb.equal(root.get(CampaignFormMeta.UUID), formUUid));

        CampaignFormMetaGeographyLevel level = em.createQuery(cq).getSingleResult();
        return level == null ? null : level.name();
    }

	
//	@Override
//	public CampaignFormMeta getByUuidAndFormVersionUuid(String uuid, String formVersionUuid) {
//
//	    if (uuid == null || formVersionUuid == null) {
//	        return null;
//	    }
//
//	    CriteriaBuilder cb = em.getCriteriaBuilder();
//	    CriteriaQuery<CampaignFormMeta> cq = cb.createQuery(CampaignFormMeta.class);
//	    Root<CampaignFormMeta> from = cq.from(CampaignFormMeta.class);
//
//	    // Define parameters
//	    ParameterExpression<String> uuidParam = cb.parameter(String.class, AbstractDomainObject.UUID);
//	    ParameterExpression<String> formVersionUuidParam = cb.parameter(String.class, "formversionuuid");
//
//	    // Build the query
//	    cq.where(
//	        cb.equal(from.get(AbstractDomainObject.UUID), uuidParam),
//	        cb.equal(from.get(CampaignFormMeta.FORMGROUPUUID), formVersionUuidParam)
//	    );
//
//	    // Create and execute the query
//	    TypedQuery<CampaignFormMeta> q = em.createQuery(cq)
//	        .setParameter(uuidParam, uuid)
//	        .setParameter(formVersionUuidParam, formVersionUuid);
//	    
//		System.out.println("eewwwwwwwwwwwwwwwwwwwwwww" + SQLExtractor.from(em.createQuery(cq)));
//
//
//	    return q.getResultList().stream().findFirst().orElse(null);
//	}

}
