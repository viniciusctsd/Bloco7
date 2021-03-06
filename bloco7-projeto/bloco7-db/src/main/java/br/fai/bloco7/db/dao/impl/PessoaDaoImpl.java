package br.fai.bloco7.db.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import br.fai.bloco7.connection.ConnectionFactory;
import br.fai.bloco7.db.dao.PessoaDao;
import br.fai.bloco7.model.Pessoa;

@Repository
public class PessoaDaoImpl implements PessoaDao {

	@Override
	public List<Pessoa> readAll() {

		final List<Pessoa> users = new ArrayList<Pessoa>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = ConnectionFactory.getConnection();

			final String sql = "select * from pessoa P inner join Usuario U on U.id = P.usuario_id";

			preparedStatement = connection.prepareStatement(sql);

			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				final Pessoa person = new Pessoa();
				person.setId(resultSet.getLong("id"));
				person.setCelular(resultSet.getString("celular"));
				person.setSenha(resultSet.getString("senha"));
				person.setEmail(resultSet.getString("email"));
				person.setCpf(resultSet.getString("cpf"));
				person.setNome(resultSet.getString("nome"));
				person.setLogradouro(resultSet.getString("logradouro"));
				person.setBairro(resultSet.getString("bairro"));
				person.setCidadeId(resultSet.getLong("cidade_id"));
				person.setCep(resultSet.getString("cep"));
				person.setNumero(resultSet.getString("numero"));

				users.add(person);

			}

		} catch (final Exception e) {

		} finally {
			ConnectionFactory.close(resultSet, preparedStatement, connection);
		}

		return users;

	}

	@Override
	public Pessoa readById(final Long id) {

		Pessoa person = null;

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = ConnectionFactory.getConnection();

			String sql = "select * from pessoa P inner join Usuario U on U.id = P.usuario_id";
			sql += " where U.id = ?";

			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setLong(1, id);

			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				person = new Pessoa();
				person.setId(resultSet.getLong("id"));
				person.setCelular(resultSet.getString("celular"));
				person.setSenha(resultSet.getString("senha"));
				person.setEmail(resultSet.getString("email"));
				person.setCpf(resultSet.getString("cpf"));
				person.setNome(resultSet.getString("nome"));
				person.setLogradouro(resultSet.getString("logradouro"));
				person.setBairro(resultSet.getString("bairro"));
				person.setCidadeId(resultSet.getLong("cidade_id"));
				person.setCep(resultSet.getString("cep"));
				person.setNumero(resultSet.getString("numero"));
			}

		} catch (final Exception e) {

		} finally {
			ConnectionFactory.close(resultSet, preparedStatement, connection);
		}

		return person;
	}

	@Override
	public Long create(final Pessoa entity) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;
		ResultSet resultSet = null;

		final String sql = " INSERT INTO usuario (senha, email, celular) values (? , ? , ?);";

		final String sql2 = "INSERT INTO pessoa (usuario_id , cpf, nome, logradouro, bairro, cidade_id, tipo ) values (? , ? , ? , ? , ? , ? , ? );";

		Long id = Long.valueOf(-1);

		try {

			connection = ConnectionFactory.getConnection();
			connection.setAutoCommit(false);

			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			preparedStatement.setString(1, entity.getSenha());
			preparedStatement.setString(2, entity.getEmail());
			preparedStatement.setString(3, entity.getCelular());

			preparedStatement.execute();
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet.next()) {
				id = resultSet.getLong(1);
			}

			connection.commit();

			preparedStatement2 = connection.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);

			preparedStatement2.setLong(1, id);
			preparedStatement2.setString(2, entity.getCpf());
			preparedStatement2.setString(3, entity.getNome());
			preparedStatement2.setString(4, entity.getLogradouro());
			preparedStatement2.setString(5, entity.getBairro());
			preparedStatement2.setLong(6, entity.getCidadeId());
			preparedStatement2.setString(7, "USUARIO");
			preparedStatement2.execute();
			connection.commit();

		} catch (final Exception e) {
			System.out.println(e.getMessage());

			try {
				connection.rollback();
			} catch (final SQLException e1) {
				System.out.println(e1.getMessage());
			}
		} finally {
			ConnectionFactory.close(preparedStatement, connection);

		}

		return id;

	}

	@Override
	public boolean update(final Pessoa entity) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;

		String sql = "UPDATE usuario SET";
		sql += " email = ?,";
		sql += " celular = ?";
		sql += " where id = ?;";

		String sql2 = "UPDATE pessoa SET";
		sql2 += " nome = ?,";
		sql2 += " logradouro = ?,";
		sql2 += " bairro = ?,";
		sql2 += " cidade_id = ?";
		sql2 += " where usuario_id = ?;";

		try {

			connection = ConnectionFactory.getConnection();
			connection.setAutoCommit(false);

			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			preparedStatement.setString(1, entity.getEmail());
			preparedStatement.setString(2, entity.getCelular());
			preparedStatement.setLong(3, entity.getId());
			preparedStatement.execute();
			connection.commit();

			preparedStatement2 = connection.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);

			preparedStatement2.setString(1, entity.getNome());
			preparedStatement2.setString(2, entity.getLogradouro());
			preparedStatement2.setString(3, entity.getBairro());
			preparedStatement2.setLong(4, entity.getCidadeId());
			preparedStatement2.setLong(5, entity.getId());
			preparedStatement2.execute();

			connection.commit();
			return true;

		} catch (final Exception e) {

			try {
				connection.rollback();
			} catch (final SQLException e1) {
				System.out.println(e1.getMessage());

			}
			return false;
		} finally {
			ConnectionFactory.close(preparedStatement, connection);
		}
	}

	@Override
	public boolean delete(final Long id) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		String sql = "START TRANSACTION;";
		sql += "DELETE FROM pessoa WHERE usuario_id = ?;";
		sql += "DELETE FROM usuario WHERE id = ?;";
		sql += "COMMIT;";

		try {

			connection = ConnectionFactory.getConnection();
			connection.setAutoCommit(false);

			preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, id);

			preparedStatement.execute();

			connection.commit();

			return true;

		} catch (final Exception e) {

			try {
				connection.rollback();
			} catch (final SQLException e1) {
				System.out.println(e1.getMessage());

			}
			return false;
		} finally {
			ConnectionFactory.close(preparedStatement, connection);
		}
	}

//	@Override
//	public Pessoa authentication(final Pessoa entity) {
//
//		Pessoa pessoa = null;
//
//		Connection connection = null;
//		PreparedStatement preparedStatement = null;
//		ResultSet resultSet = null;
//
//		String sql = "select * from usuario where ";
//		sql += " email = ? ";
//		sql += " and senha = ?";
//
//		try {
//
//			connection = ConnectionFactory.getConnection();
//
//			preparedStatement = connection.prepareStatement(sql);
//
//			preparedStatement.setString(1, entity.getEmail());
//			preparedStatement.setString(2, entity.getSenha());
//
//			resultSet = preparedStatement.executeQuery();
//
//			if (resultSet.next()) {
//				pessoa = new Pessoa();
//				pessoa.setId(resultSet.getLong("id"));
//				return pessoa;
//			}
//
//		} catch (final Exception e) {
//			System.out.println(e.getMessage());
//		} finally {
//			ConnectionFactory.close(resultSet, preparedStatement, connection);
//		}
//		return null;
//	}

	@Override
	public boolean updatePassword(final Pessoa entity) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		String sql = "UPDATE usuario SET";
		sql += " senha = ?";
		sql += " where id = ?;";

		try {

			connection = ConnectionFactory.getConnection();
			connection.setAutoCommit(false);

			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			preparedStatement.setString(1, entity.getSenha());
			preparedStatement.setLong(2, entity.getId());
			preparedStatement.execute();

			connection.commit();
			return true;

		} catch (final Exception e) {
			System.out.println(e.getMessage());
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				System.out.println(e1.getMessage());

			}
			return false;
		} finally {
			ConnectionFactory.close(preparedStatement, connection);
		}
	}

	@Override
	public Pessoa validadeEmailAndPassword(final String email, final String password) {
		Pessoa pessoa = null;

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {

			connection = ConnectionFactory.getConnection();

			final String sql = "select * from pessoa P inner join Usuario U on U.id = P.usuario_id WHERE email = ? AND senha = ?;";

			preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setString(1, email);
			preparedStatement.setString(2, password);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {

				pessoa = new Pessoa();
				pessoa.setId(resultSet.getLong("id"));
				pessoa.setCelular(resultSet.getString("celular"));
				pessoa.setSenha(resultSet.getString("senha"));
				pessoa.setEmail(resultSet.getString("email"));
				pessoa.setCpf(resultSet.getString("cpf"));
				pessoa.setNome(resultSet.getString("nome"));
				pessoa.setLogradouro(resultSet.getString("logradouro"));
				pessoa.setBairro(resultSet.getString("bairro"));
				pessoa.setCidadeId(resultSet.getLong("cidade_id"));
				pessoa.setCep(resultSet.getString("cep"));
				pessoa.setNumero(resultSet.getString("numero"));
				pessoa.setTipo(resultSet.getString("tipo"));

			}

		} catch (final Exception e) {

			System.out.println(e.getMessage());

		} finally {

			ConnectionFactory.close(resultSet, preparedStatement, connection);
		}
		return pessoa;
	}

}
