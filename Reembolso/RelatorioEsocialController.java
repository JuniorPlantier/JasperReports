package sp.senac.br.customrhev.controllers;

import static net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream;
import static net.sf.jasperreports.engine.JasperFillManager.fillReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import sp.senac.br.customrhev.entities.VwCriticaEsocial;
import sp.senac.br.customrhev.services.RelatorioEsocialService;

@Controller
@RequestMapping("/relatorioEsocial")
@SessionAttributes({ "rhevPath" })
public class RelatorioEsocialController {

	@Autowired
	private DataSource dbsource;

	@Autowired
	ServletContext context;
	
	@Value("${environment}")
	private String ambiente;

	@Value("${usuario.desenv}")
	private String usuarioDesenv;

	@Value("${rhevpath}")
	private String rhevPath;
	
	@Autowired
	private RelatorioEsocialService relatorioEsocialService;

	private static final String FILE_SEPARATOR = java.io.File.separator;

	private static final String PATH_REL_VIEW_INT = "jasper" + FILE_SEPARATOR + "criticaEsocial" + FILE_SEPARATOR + "CriticaEsocialVerbaSenacInt.jasper";
	private static final String PATH_REL_VERBAS_MOV = "jasper" + FILE_SEPARATOR + "criticaEsocial" + FILE_SEPARATOR + "RelatorioEsocialVerbasMovimentos.jasper";
	
	private static final String PATH_REL_FORN_PAG = "jasper" + FILE_SEPARATOR + "criticaEsocial" + FILE_SEPARATOR + "RelEsocialFornecedoresPagamento.jasper";
	private static final String PATH_REL_FORN_RHEV = "jasper" + FILE_SEPARATOR + "criticaEsocial" + FILE_SEPARATOR + "RelEsocialFornecedoresNaoEncontradosRhev.jasper";
	private static final String PATH_REL_FORN_MOV_SYN = "jasper" + FILE_SEPARATOR + "criticaEsocial" + FILE_SEPARATOR + "RelEsocialFornecedoresVerbasMovimentoSynchro.jasper";

	private static final List<VwCriticaEsocial> listaQtd = new ArrayList<>();
	private static final List<VwCriticaEsocial> listaDif = new ArrayList<>();
	private static final List<VwCriticaEsocial> listaVlr = new ArrayList<>();
	
	@InitBinder
	public void allowEmptyDateBinding(WebDataBinder binder) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		simpleDateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(simpleDateFormat, true));
	}

	@GetMapping
	public String relatorios(ModelMap model) {
		// Título da página
		model.addAttribute("nomeRelatorio", "Relatórios de Apoio eSocial");
		
		// Críticas que aparecem na página
		List<VwCriticaEsocial> listaRelatorios = relatorioEsocialService.listarCritcas();
		organizaRelatorios(listaRelatorios);
		model.addAttribute("CriticaQtd", listaQtd);
		model.addAttribute("CriticaDif", listaDif);
		model.addAttribute("CriticaVlr", listaVlr);
		
 
		//model.addAttribute("tipoColaborador", this.relatorioService.listarTipoColaborador());
		//model.addAttribute("filtro", new FiltroDtoRel());
		return "relatorios/relatorioEsocial";
	}
	
	@GetMapping("/imprimir/{tipoCritica}")
	public void imprimir(HttpServletRequest request, @PathVariable("tipoCritica") String tipoCritica, HttpServletResponse response) {

		String reportPath = null;
		JasperPrint jasperPrint = null;
		ServletOutputStream out1 = null;
		
		switch (tipoCritica.toUpperCase()) {
		case ("VLRVIEWSENACINT"):
			reportPath = context.getRealPath(PATH_REL_VIEW_INT);
			break;
		case ("RELVERBASESOCIAL"):
			reportPath = context.getRealPath(PATH_REL_VERBAS_MOV);
			break;
		case ("FORNPAGAMENTO"):
			reportPath = context.getRealPath(PATH_REL_FORN_PAG);
			break;
		case ("FORNRHEV"):
			reportPath = context.getRealPath(PATH_REL_FORN_RHEV);
			break;
		case ("FORNMOVIMENTOSSYNCHRO"):
			reportPath = context.getRealPath(PATH_REL_FORN_MOV_SYN);
			break;
		default:
			response.setStatus(404);
			break;
		}
		
		try (Connection connection = dbsource.getConnection();
			 ByteArrayOutputStream baos = new ByteArrayOutputStream(); 		    
			) 
		{
			jasperPrint = fillReport(reportPath, null, connection);

			response.setContentType("application/pdf");
			response.setHeader("Refresh", "5; url=/");
			response.setHeader("Content-Disposition", "attachment; filename="+tipoCritica+".pdf");

			exportReportToPdfStream(jasperPrint, baos);

			response.setContentLength(baos.size());
			out1 = response.getOutputStream();

			baos.writeTo(out1);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JRException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void organizaRelatorios(List<VwCriticaEsocial> relatorios) {
		
		for (VwCriticaEsocial relatorio : relatorios) {
			if (relatorio.getid().toUpperCase().startsWith("QUANTIDADE")) {
				listaQtd.add(relatorio);
			} else if (relatorio.getid().toUpperCase().startsWith("DIFERENÇA")) {
				listaDif.add(relatorio);
			} else if (relatorio.getid().toUpperCase().startsWith("VALOR")) {
				listaVlr.add(relatorio);
			}
		}
	}
	
}