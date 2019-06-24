package sp.senac.br.reembolso.controllers;

import static net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream;
import static net.sf.jasperreports.engine.JasperFillManager.fillReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import sp.senac.br.reembolso.dtos.FavorecidoDto;
import sp.senac.br.reembolso.dtos.RelatorioDto;
import sp.senac.br.reembolso.dtos.SearchDto;
import sp.senac.br.reembolso.dtos.UsuarioDto;
import sp.senac.br.reembolso.entities.FormasPagamento;
import sp.senac.br.reembolso.entities.Unidades;
import sp.senac.br.reembolso.enums.StatusPrestacaoEnum;
import sp.senac.br.reembolso.repositories.FormasPagamentoRepository;
import sp.senac.br.reembolso.repositories.UnidadesRepository;
import sp.senac.br.reembolso.services.FavorecidoService;
import sp.senac.br.reembolso.services.LancamentoHeaderService;

@Controller("/reembolso/relatorio/")
@SessionAttributes({ "usuario", "menus"})
public class RelatorioController {

	@Autowired
	private DataSource dbsource;

	@Autowired
	ServletContext context;

	@Autowired
	private MainController mainController;

	@Autowired
	private FavorecidoService favorecidoService;

	@Autowired
	private FormasPagamentoRepository formasPagamentoRepository;

	@Autowired
	private LancamentoHeaderService lancamentoHeaderService;

	@Autowired
	private UnidadesRepository unidadesRepository;

	private static final String FILE_SEPARATOR = java.io.File.separator;

	private static final String PATH_ADIANTAMENTO="jasper" + FILE_SEPARATOR + "adiantamento" + FILE_SEPARATOR + "adiantamento.jasper"; 
	private static final String PATH_REEMBOLSO="jasper" + FILE_SEPARATOR + "reembolso" + FILE_SEPARATOR + "reembolso.jasper";
	private static final String PATH_SUB_REEMBOLSO_DIR="jasper" + FILE_SEPARATOR + "reembolso" + FILE_SEPARATOR;
	private static final String PATH_PRESTACAO_CONTA="jasper" + FILE_SEPARATOR + "prestacao" + FILE_SEPARATOR + "prestacao.jasper";
	private static final String PATH_SUB_PRESTACAO_CONTA_DIR="jasper" + FILE_SEPARATOR + "prestacao" + FILE_SEPARATOR;
	private static final String PATH_REL_CONCILIACAO="jasper" + FILE_SEPARATOR + "conciliacao" + FILE_SEPARATOR + "conciliacao.jasper";
	private static final String PATH_REL_FECHAMENTO="jasper" + FILE_SEPARATOR + "fechamento" + FILE_SEPARATOR + "fechamento.jasper";
	private static final String PATH_REL_BENEFICIOS="jasper" + FILE_SEPARATOR + "beneficios" +  FILE_SEPARATOR + "beneficios.jasper";
	private static final String PATH_SUB_BENEFICIOS_DIR="jasper" + FILE_SEPARATOR + "beneficios";

	private static final Logger LOGGER = LoggerFactory.getLogger(RelatorioController.class);

	@GetMapping(value = "/imprimir/{id}/{tipo}/{codigo}")
	public void imprimir(HttpServletRequest request, @PathVariable("id") String idSt, @PathVariable("tipo") Integer tipo, @PathVariable("codigo") String codigo, HttpServletResponse response) {
		Connection connection = null;

		Long id = Long.valueOf(idSt);

		Map<String, Object> parametros = new HashMap<>();
		UsuarioDto usuario = mainController.getUsuarioSession(request);
		String logoPath = context.getRealPath("img" + FILE_SEPARATOR + "senac.jpg");
		String reportPath = null;
		String subReportPath = null;
		JasperPrint jasperPrint = null;
		ByteArrayOutputStream baos = null;
		ServletOutputStream out1 = null;

		switch (tipo) {
		case (1): //ADIANTAMENTO
			reportPath = context.getRealPath(PATH_ADIANTAMENTO);
		break;
		case (2): //PRESTACAO CONTA
			reportPath = context.getRealPath(PATH_PRESTACAO_CONTA);
		subReportPath = context.getRealPath(PATH_SUB_PRESTACAO_CONTA_DIR);
		break;
		case (3): //REEMBOLSO
			reportPath = context.getRealPath(PATH_REEMBOLSO);
		subReportPath = context.getRealPath(PATH_SUB_REEMBOLSO_DIR);
		break;
		case (4): //BENEFÍCIOS
			reportPath = context.getRealPath(PATH_REL_BENEFICIOS);
		subReportPath = context.getRealPath(PATH_SUB_BENEFICIOS_DIR);
		break;
		default:
			response.setStatus(404);
			break;
		}

		try {
			connection = dbsource.getConnection();

			parametros.put("ID_LANCAMENTO", id);
			parametros.put("IMG_LOGO", logoPath);
			parametros.put("SUBREPORT_DIR", subReportPath + FILE_SEPARATOR); 

			jasperPrint = fillReport(reportPath, parametros, connection);

			baos = new ByteArrayOutputStream();

			response.setContentType("application/pdf");
			response.setHeader("Refresh", "5; url=/");
			response.setHeader("Content-Disposition", "attachment; filename="+codigo+".pdf");

			exportReportToPdfStream(jasperPrint, baos);

			response.setContentLength(baos.size());
			out1 = response.getOutputStream();

			baos.writeTo(out1);

			lancamentoHeaderService.atualizarStatus(StatusPrestacaoEnum.IMPRESSO.getCodigo(), id, usuario.getChapa());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JRException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(out1 != null){
				try {
					out1.close();
					out1.flush();
				} catch (IOException e) {
					LOGGER.error(RelatorioController.class.getName(), e);
				}
			}
			if(baos != null){
				try {
					baos.close();
					baos.flush();
				} catch (IOException e) {
					LOGGER.error(RelatorioController.class.getName(), e);
				}
			}
			try {
				if(!connection.isClosed()){
					connection.close();
				}
			} catch (SQLException e) {
				LOGGER.error(RelatorioController.class.getName(), e);
			}
			if (!request.isAsyncStarted() && !response.isCommitted()) {
				try {
					response.flushBuffer();
				} catch (IOException e) {
					LOGGER.error(RelatorioController.class.getName(), e);
				}
			}
		}
	}

	@GetMapping(value = "/relatorio")
	public ModelAndView relatorioConciliacaoGet(@ModelAttribute RelatorioDto relatorioDto, HttpServletRequest request) {
		relatorioDto = new RelatorioDto();
		UsuarioDto usuario = mainController.getUsuarioSession(request);
		SearchDto search = new SearchDto(usuario.getChapa().toString());
		FavorecidoDto favorecido = favorecidoService.getFavorecido(search);

		List<FormasPagamento> formasPagamento = formasPagamentoRepository.listarFormasPagamento();
		List<Unidades> unidades = this.unidadesRepository.listaUnidades();

		if(favorecido.getOrgId() != 1756) {
			unidades.removeIf(u -> u.getOrgId().compareTo(favorecido.getOrgId()) != 0);
			relatorioDto.setUnidade(unidades.get(0).getSiglaUo());
		}

		ModelAndView mav = new ModelAndView("relatorio" ,"relatorio-entity" , relatorioDto);
		mav.addObject("unidadesMap", unidades);
		mav.addObject("formasPagamentoMap", formasPagamento);
		mav.addObject("isSede", new Boolean(favorecido.getOrgId() == 1756));

		return mav;
	}

	@PostMapping(value = "/relatorio")
	public void relatorioConciliacaoPost(@ModelAttribute RelatorioDto relatorioDto, HttpServletRequest request, HttpServletResponse response) {
		Connection connection = null;

		Map<String, Object> parametros = new HashMap<>();
		String logoPath = context.getRealPath("img" + FILE_SEPARATOR + "senac.jpg");
		String reportPath = null;
		String nome = null;
		JasperPrint jasperPrint = null;
		ByteArrayOutputStream baos = null;
		ServletOutputStream out1 = null;

		switch (relatorioDto.getTipoRelatorio()) {
		case 1:
			reportPath = context.getRealPath(PATH_REL_CONCILIACAO);
			nome = "conciliacao";
			break;
		case 2:
			reportPath = context.getRealPath(PATH_REL_FECHAMENTO);
			nome = "fechamento";
			break;
		default:
			break;
		}

		try {
			connection = dbsource.getConnection();

			parametros.put("FORMA_PAGAMENTO", relatorioDto.getFormaPagamento());
			parametros.put("UNIDADE", relatorioDto.getUnidade()==null?"":relatorioDto.getUnidade());
			parametros.put("DATA_INICIAL", relatorioDto.getDataInicio());
			parametros.put("DATA_FINAL", relatorioDto.getDataFim());
			parametros.put("ID_TIPO_LANCAMENTO", relatorioDto.getIdTipoLancamento());
			parametros.put("IMG_LOGO", logoPath);

			jasperPrint = fillReport(reportPath, parametros, connection);

			baos = new ByteArrayOutputStream();

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=" + nome + ".pdf");

			exportReportToPdfStream(jasperPrint, baos);

			response.setContentLength(baos.size());
			out1 = response.getOutputStream();

			baos.writeTo(out1);
		} catch (IOException | JRException | SQLException e) {
			LOGGER.error(RelatorioController.class.getName(), e);
		} finally {
			if(out1 != null){
				try {
					out1.close();
					out1.flush();
				} catch (IOException e) {
					LOGGER.error(RelatorioController.class.getName(), e);
				}
			}
			if(baos != null){
				try {
					baos.close();
					baos.flush();
				} catch (IOException e) {
					LOGGER.error(RelatorioController.class.getName(), e);
				}
			}
			try {
				if(!connection.isClosed()){
					connection.close();
				}
			} catch (SQLException e) {
				LOGGER.error(RelatorioController.class.getName(), e);
			}
			if (!request.isAsyncStarted() && !response.isCommitted()) {
				try {
					response.flushBuffer();
				} catch (IOException e) {
					LOGGER.error(RelatorioController.class.getName(), e);
				}
			}
		}
	}
}
