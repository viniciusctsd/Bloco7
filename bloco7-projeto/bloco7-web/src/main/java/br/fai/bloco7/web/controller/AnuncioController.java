package br.fai.bloco7.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.fai.bloco7.model.Anuncio;
import br.fai.bloco7.model.Cidade;
import br.fai.bloco7.model.Pessoa;
import br.fai.bloco7.web.security.provider.Bloco7AuthenticationProvider;
import br.fai.bloco7.web.service.AnuncioService;
import br.fai.bloco7.web.service.CidadeService;
import br.fai.bloco7.web.service.PessoaService;
import br.fai.bloco7.web.service.ReportService;

@Controller
@RequestMapping("/anuncios")
public class AnuncioController {

	@Autowired
	private AnuncioService anuncioService;

	@Autowired
	private PessoaService pessoaService;

	@Autowired
	private PessoaController pessoaController;

	@Autowired
	private CidadeService cidadeService;

	@Autowired
	private Bloco7AuthenticationProvider authenticationProvider;

	@Autowired
	private ReportService reportService;

	@GetMapping("/register")
	public String getRegisterPage(final Anuncio anuncio, final Model model) {

//		if (PessoaController.idLogado == null) {
//			return "redirect:/pessoa/login";
//		}
//		model.addAttribute("idUsuarioLogado", PessoaController.idLogado);
		return "anuncio/register";
	}

//	Metodo para exibição da pagina de anuncios
	@GetMapping("/listar")
	public String getAnuncioGridPage(final Anuncio pesquisa, final Model model) {

		final List<Anuncio> anuncios = anuncioService.readAll();

//		model.addAttribute("idUsuarioLogado", PessoaController.idLogado);
		model.addAttribute("listaDeAnuncio", anuncios);
		model.addAttribute("activePage", "anuncio");

		return "anuncio/anuncios-grid";
	}

//	Metodo para detalhes de anuncio especifico
	@GetMapping("/detalhes/{id}")
	public String getDetailPage(@PathVariable("id") final long idAnuncio, final Model model) {

		final Anuncio anuncio = anuncioService.readById(idAnuncio);
		model.addAttribute("anuncio", anuncio);

		final Pessoa pessoa = pessoaService.readById(anuncio.getUsuarioAnuncianteId());
		model.addAttribute("pessoa", pessoa);

		final Cidade cidade = cidadeService.readById(anuncio.getCidadeId());
		model.addAttribute("cidade", cidade);

		model.addAttribute("activePage", "anuncio");

		final Pessoa pessoaLogada = authenticationProvider.getAuthenticatedUser();
		model.addAttribute("idLogado", pessoaLogada.getId());

		return "anuncio/anuncio-single";
	}

	@GetMapping("/edit/{id}")
	public String getEditPage(@PathVariable("id") final long id, final Model model) {

		final Anuncio anuncio = anuncioService.readById(id);

		model.addAttribute("anuncio", anuncio);
//		model.addAttribute("idUsuarioLogado", PessoaController.idLogado);

		return "anuncio/edit";
	}

	@PostMapping("/create")
	public String createAnuncio(final Anuncio anuncio, final Model model) {

//		if (PessoaController.idLogado == null) {
//			return "redirect:/pessoa/register";
//
//		} else {

//			anuncio.setUsuarioAnuncianteId(PessoaController.idLogado);
		final Pessoa pessoa = authenticationProvider.getAuthenticatedUser();
		anuncio.setUsuarioAnuncianteId(pessoa.getId());
		final Long id = anuncioService.create(anuncio);

		if (id != -1) {
//				model.addAttribute("idUsuarioLogado", PessoaController.idLogado);
			model.addAttribute("anuncio", id);
			return getDetailPage(id, model);
		} else {
//				model.addAttribute("idUsuarioLogado", PessoaController.idLogado);
			model.addAttribute("anuncio", id);
			return "anuncios/register";
		}

	}

	@PostMapping("/update")
	public String update(final Anuncio anuncio, final Model model) {

		anuncioService.update(anuncio);

		return getDetailPage(anuncio.getId(), model);
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable("id") final Long id, final Model model, final Anuncio anuncio) {

		anuncioService.deleteById(id);

//		return pessoaController.getDetailPage(pessoaController.idLogado, model);
		return pessoaController.getDetailPage(model);
	}

//	Metodo para realizar pesquisa
	@PostMapping("/pesquisar")
	public String readByCriteria(final Anuncio anuncio, final Model model) {

		// Cria lista do tipo anuncios para receber o resultado do metodo pesquisar()
		final List<Anuncio> anuncios = anuncioService.readByCriteria(anuncio);

		// Injeta o resultado da pesquisa na view
		model.addAttribute("resultPesquisa", anuncios);

		// Indicador de pagina ativa
		model.addAttribute("activePage", "anuncio");

//		model.addAttribute("idUsuarioLogado", PessoaController.idLogado);

		// Retorna pagina de pesquisa
		return "anuncio/anuncios-pesquisa";
	}

	@GetMapping("/report/read-all")
	public ResponseEntity<byte[]> getAllUsersReport() {

		final String filePath = reportService.generateAndGetPdfFilePath();

		if (filePath.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}

		final File pdfFile = Paths.get(filePath).toFile();

		try {
			final byte[] fileContent = Files.readAllBytes(pdfFile.toPath());

			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/pdf"));

//			headers.add("Content-Disposition", "attachment; filename=" + pdfFile.getName());
			headers.add("Content-Disposition", "inline; filename=" + pdfFile.getName());
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);

		} catch (final IOException e) {

			System.out.println(e.getMessage());

			return ResponseEntity.badRequest().build();
		}

	}

}
