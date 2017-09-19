package com.hsbcprivatebank.efos.global.pb.base.service.session;
import com.hsbcprivatebank.efos.global.pb.base.common.exception.ApplicationException;
import com.hsbcprivatebank.efos.global.pb.base.service.dto.QuotationOrdeDTO;
import com.hsbcprivatebank.efos.global.pb.base.service.form.QuotationCommonInputForm;
import com.hsbcprivatebank.efos.global.pb.base.service.form.QuotationInputForm;
/**
 * Local interface for Enterprise Bean: QuotationService
 */
public interface QuotationServiceLocal extends javax.ejb.EJBLocalObject
{
	public void acknowledgeOrder(QuotationCommonInputForm form) 
        throws ApplicationException;
        
	public void quoteRate(QuotationCommonInputForm form) 
        throws ApplicationException;
        
	public void riskRate(QuotationCommonInputForm form) 
        throws ApplicationException;
        
	public QuotationOrdeDTO confirmOrder(QuotationInputForm form) 
        throws ApplicationException;
        
	public void refuseOrder(QuotationCommonInputForm form) 
        throws ApplicationException;
        
	public QuotationOrdeDTO getQuotationOrderList(QuotationCommonInputForm form) 
        throws ApplicationException;
        
    public void closePosition(QuotationInputForm form) 
        throws ApplicationException;
}
